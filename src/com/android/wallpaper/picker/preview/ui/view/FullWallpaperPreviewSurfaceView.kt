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
package com.android.wallpaper.picker.preview.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import com.android.wallpaper.util.WallpaperCropUtils

/**
 * A [SurfaceView] for wallpaper preview that scales the surface up according to
 * [WallpaperCropUtils.getSystemWallpaperMaximumScale]
 */
// TODO(b/303317694): current preview crop is inaccurate, fix to respect wallpaper parallax zoom
class FullWallpaperPreviewSurfaceView(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val scale = WallpaperCropUtils.getSystemWallpaperMaximumScale(context)
        setMeasuredDimension((measuredWidth * scale).toInt(), (measuredHeight * scale).toInt())
    }
}
