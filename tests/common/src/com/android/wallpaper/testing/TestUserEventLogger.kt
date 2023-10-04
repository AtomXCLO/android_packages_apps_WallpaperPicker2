/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.wallpaper.testing

import android.content.Intent
import com.android.wallpaper.module.logging.UserEventLogger
import com.android.wallpaper.module.logging.UserEventLogger.EffectStatus

/** Test implementation of [UserEventLogger]. */
open class TestUserEventLogger : UserEventLogger {

    val numWallpaperSetEvents = 0
    var numWallpaperSetResultEvents = 0
        private set

    override fun logAppLaunched(launchSource: Intent) {}

    override fun logActionClicked(collectionId: String, actionLabelResId: Int) {}

    override fun logSnapshot() {}

    override fun logWallpaperApplied(
        collectionId: String?,
        wallpaperId: String?,
        effects: String?
    ) {}

    override fun logEffectApply(
        effect: String,
        status: Int,
        timeElapsedMillis: Long,
        resultCode: Int
    ) {}

    override fun logEffectProbe(effect: String, @EffectStatus status: Int) {}

    override fun logEffectForegroundDownload(
        effect: String,
        status: Int,
        timeElapsedMillis: Long
    ) {}
}
