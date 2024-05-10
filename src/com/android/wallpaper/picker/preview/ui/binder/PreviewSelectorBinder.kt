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
package com.android.wallpaper.picker.preview.ui.binder

import android.content.Context
import android.graphics.Point
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.transition.Transition
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.android.wallpaper.picker.preview.ui.fragment.smallpreview.adapters.TabTextPagerAdapter
import com.android.wallpaper.picker.preview.ui.viewmodel.FullPreviewConfigViewModel
import com.android.wallpaper.picker.preview.ui.viewmodel.WallpaperPreviewViewModel

/** Binds and synchronizes the tab and preview view pagers. */
object PreviewSelectorBinder {

    fun bind(
        tabsViewPager: ViewPager,
        previewsViewPager: ViewPager2,
        previewDisplaySize: Point,
        wallpaperPreviewViewModel: WallpaperPreviewViewModel,
        applicationContext: Context,
        viewLifecycleOwner: LifecycleOwner,
        currentNavDestId: Int,
        transition: Transition?,
        transitionConfig: FullPreviewConfigViewModel?,
        navigate: (View) -> Unit,
    ) {
        // set up tabs view pager
        TabPagerBinder.bind(tabsViewPager)

        // set up previews view pager
        PreviewPagerBinder.bind(
            applicationContext,
            viewLifecycleOwner,
            previewsViewPager,
            wallpaperPreviewViewModel,
            previewDisplaySize,
            currentNavDestId,
            transition,
            transitionConfig,
            navigate,
        )

        // synchronize the two pagers
        synchronizePreviewAndTabsPager(tabsViewPager, previewsViewPager)
        tabsViewPager.currentItem =
            (tabsViewPager.adapter as TabTextPagerAdapter).getPageNumber(
                wallpaperPreviewViewModel.isViewAsHome
            )
    }

    private fun synchronizePreviewAndTabsPager(
        tabsViewPager: ViewPager,
        previewsViewPager: ViewPager2,
    ) {
        val onPageChangeListener =
            object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    previewsViewPager.setCurrentItem(position, true)
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {}

                override fun onPageScrollStateChanged(state: Int) {}
            }

        tabsViewPager.addOnPageChangeListener(onPageChangeListener)

        previewsViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabsViewPager.setCurrentItem(position, true)
                }
            }
        )
    }
}
