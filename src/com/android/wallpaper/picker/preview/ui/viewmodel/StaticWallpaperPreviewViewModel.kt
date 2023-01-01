/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.wallpaper.picker.preview.ui.viewmodel

import android.app.WallpaperColors
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.Point
import android.graphics.Rect
import com.android.wallpaper.asset.Asset
import com.android.wallpaper.dispatchers.BackgroundDispatcher
import com.android.wallpaper.model.WallpaperInfo
import com.android.wallpaper.model.wallpaper.ScreenOrientation
import com.android.wallpaper.model.wallpaper.WallpaperModel.StaticWallpaperModel
import com.android.wallpaper.module.WallpaperPreferences
import com.android.wallpaper.picker.preview.ui.WallpaperPreviewActivity
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** View model for static wallpaper preview used in [WallpaperPreviewActivity] and its fragments */
@ViewModelScoped
class StaticWallpaperPreviewViewModel
@Inject
constructor(
    private val wallpaperPreferences: WallpaperPreferences,
    @BackgroundDispatcher private val bgDispatcher: CoroutineDispatcher,
) {
    /** The state of static wallpaper crop in full preview, before user confirmation. */
    var fullPreviewCrop: Rect? = null

    private var initialized = false

    private val _lowResBitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val lowResBitmap: Flow<Bitmap> = _lowResBitmap.filterNotNull()

    private val _cropHints: MutableStateFlow<Map<ScreenOrientation, Rect>?> = MutableStateFlow(null)
    private val _cachedWallpaperColors: MutableStateFlow<WallpaperColors?> = MutableStateFlow(null)
    private val croppedBitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val wallpaperColors: Flow<WallpaperColors> =
        merge(
            _cachedWallpaperColors.filterNotNull(),
            croppedBitmap
                .filterNotNull()
                .map { it.extractColors() }
                .filterNotNull()
                .flowOn(bgDispatcher),
        )

    // Wallpaper ID is required to cache the wallpaper colors to the preferences
    private var wallpaperId: String? = null
    private val wallpaperAsset: MutableStateFlow<Asset?> = MutableStateFlow(null)

    val subsamplingScaleImageViewModel: Flow<FullResWallpaperViewModel> =
        wallpaperAsset
            .filterNotNull()
            .combine(_cropHints) { asset, cropHints ->
                val dimensions = asset.decodeRawDimensions()
                asset.decodeBitmap(dimensions)?.let { bitmap ->
                    if (_cachedWallpaperColors.value == null && wallpaperId != null) {
                        // If no cached colors from the preferences, extra colors from the original
                        // bitmap and cache them to the preferences.
                        bitmap.extractColors()?.also { colors ->
                            _cachedWallpaperColors.value = colors
                            wallpaperPreferences.storeWallpaperColors(
                                wallpaperId,
                                colors,
                            )
                        }
                    }
                    FullResWallpaperViewModel(bitmap, dimensions, cropHints)
                }
            }
            .filterNotNull()
            .flowOn(bgDispatcher)

    /**
     * Init function for setting the wallpaper info that is retrieved from the intent bundle when
     * onCreate() in Activity or Fragment.
     */
    suspend fun initializeViewModel(
        context: Context,
        wallpaper: WallpaperInfo,
        staticModel: StaticWallpaperModel
    ) {
        val appContext = context.applicationContext
        if (!initialized) {
            _cropHints.value = staticModel.staticWallpaperData.cropHints
            val asset: Asset? = wallpaper.getAsset(appContext)
            val id: String? = wallpaper.getStoredWallpaperId(appContext)
            wallpaperAsset.value = asset
            wallpaperId = id
            id?.let { wallpaperPreferences.getWallpaperColors(it) }
                ?.run { _cachedWallpaperColors.value = this }
            withContext(bgDispatcher) { _lowResBitmap.value = asset?.getLowResBitmap(appContext) }
            initialized = true
        }
    }

    fun updateCropHints(cropHints: Map<ScreenOrientation, Rect>) {
        _cropHints.value = _cropHints.value?.plus(cropHints) ?: cropHints
    }

    // TODO b/296288298 Create a util class for Bitmap and Asset
    private suspend fun Asset.decodeRawDimensions(): Point =
        suspendCancellableCoroutine { k: CancellableContinuation<Point> ->
            val callback =
                Asset.DimensionsReceiver { it?.let { k.resumeWith(Result.success(Point(it))) } }
            decodeRawDimensions(null, callback)
        }

    // TODO b/296288298 Create a util class functions for Bitmap and Asset
    private suspend fun Asset.decodeBitmap(dimensions: Point): Bitmap? =
        suspendCancellableCoroutine { k: CancellableContinuation<Bitmap?> ->
            val callback = Asset.BitmapReceiver { k.resumeWith(Result.success(it)) }
            decodeBitmap(dimensions.x, dimensions.y, callback)
        }

    // TODO b/296288298 Create a util class functions for Bitmap and Asset
    private fun Bitmap.extractColors(): WallpaperColors? {
        val tmpOut = ByteArrayOutputStream()
        var shouldRecycle = false
        var cropped = this
        if (cropped.compress(Bitmap.CompressFormat.PNG, 100, tmpOut)) {
            val outByteArray = tmpOut.toByteArray()
            val options = BitmapFactory.Options()
            options.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
            cropped = BitmapFactory.decodeByteArray(outByteArray, 0, outByteArray.size)
        }
        if (cropped.config == Bitmap.Config.HARDWARE) {
            cropped = cropped.copy(Bitmap.Config.ARGB_8888, false)
            shouldRecycle = true
        }
        val colors = WallpaperColors.fromBitmap(cropped)
        if (shouldRecycle) {
            cropped.recycle()
        }
        return colors
    }
}
