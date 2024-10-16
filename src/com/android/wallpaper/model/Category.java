/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.wallpaper.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.android.wallpaper.asset.Asset;

/**
 * Wallpaper category model object.
 */
public abstract class Category {
    private final String mTitle;
    private final String mCollectionId;
    private final int mPriority;
    private final boolean mIsDownloadable;

    private final String mDownloadComponent;

    /**
     * Constructs a Category object.
     *
     * @param title        Displayed title of category.
     * @param collectionId A collection ID that callers must ensure is unique among all categories.
     * @param priority     Priority (lowest number = highest priority) among all categories presented.
     */
    public Category(String title, String collectionId, int priority) {
        this(title, collectionId, priority, false, null);
    }

    /**
     * Constructs a Category object.
     *
     * @param title             Displayed title of category.
     * @param collectionId      A collection ID that callers must ensure is unique among all
     *                          categories.
     * @param priority          Priority (lowest number = highest priority) among all categories
     *                          presented.
     * @param isDownloadable    Describes if this Category has downloadable wallpapers
     * @param downloadComponent The name of the component this category uses to download wallpapers
     */
    public Category(String title, String collectionId, int priority, boolean isDownloadable,
            String downloadComponent) {
        mTitle = title;
        mCollectionId = collectionId;
        mPriority = priority;
        mIsDownloadable = isDownloadable;
        mDownloadComponent = downloadComponent;
    }

    /**
     * Returns whether this category has downloadable wallpapers
     */
    public boolean isCategoryDownloadable() {
        return mIsDownloadable;
    }

    /**
     * Returns this [Category]'s download component
     */
    public String getCategoryDownloadComponent() {
        return mDownloadComponent;
    }

    /**
     * Returns whether user created wallpapers are supported or not.
     */
    public boolean supportsUserCreatedWallpapers() {
        return false;
    }

    /**
     * Shows the UI for picking wallpapers within this category.
     *
     * @param srcActivity
     * @param requestCode Request code to pass in when starting the picker activity.
     */
    public abstract void show(Activity srcActivity, int requestCode);

    /**
     * Returns true if this Category contains an enumerable set of wallpapers which can be presented
     * by a UI enclosed in an activity. Returns false if, by contrast, this Category must be presented
     * via #show() because its contents are not enumerable.
     */
    public boolean isEnumerable() {
        return false;
    }

    /**
     * Returns true if this category contains a single Wallpaper, which could then be retrieved
     * via {@link #getSingleWallpaper()}
     */
    public boolean isSingleWallpaperCategory() {
        return false;
    }

    /**
     * If {@link #isSingleWallpaperCategory()} returned true, this method will return the single
     * wallpaper contained in this category.
     * @return a {@link WallpaperInfo} for the one wallpaper in this category, if this category is
     * a single wallpaper category, or {@code null} otherwise.
     */
    @Nullable
    public WallpaperInfo getSingleWallpaper() {
        return null;
    }

    /**
     * @return The title of the category.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * @return The ID of the collection this category represents.
     */
    public String getCollectionId() {
        return mCollectionId;
    }

    /**
     * Returns the overlay icon. Takes an application's Context if a Category needs to query for what
     * resources may be available on the device (for example, querying permissions).
     */
    public Drawable getOverlayIcon(Context unused) {
        return null;
    }

    /**
     * Returns the relative priority of the category. The lower the number, the higher the priority.
     */
    public int getPriority() {
        return mPriority;
    }

    /**
     * Returns the desired size of the overlay icon in density-independent pixels. Default value is
     * 40.
     */
    public int getOverlayIconSizeDp() {
        return 40;
    }

    /**
     * Returns the {@link WallpaperRotationInitializer} for this category or null if rotation is not
     * enabled for this category.
     */
    public WallpaperRotationInitializer getWallpaperRotationInitializer() {
        return null;
    }

    /**
     * Returns the thumbnail Asset. Takes an application's Context if a Category needs to query for
     * what resources may be available on the device (for example, querying permissions).
     */
    public abstract Asset getThumbnail(Context context);

    /**
     * Returns whether this category allows the user to pick custom photos via Android's photo picker.
     */
    public boolean supportsCustomPhotos() {
        return false;
    }

    /**
     * Returns whether this category is or contains third-party wallpapers
     */
    public boolean supportsThirdParty() {
        return false;
    }

    /**
     * Returns whether this Category contains or represents a third party wallpaper with the given
     * packageName (this only makes sense if #supportsThirdParty() returns true).
     */
    public boolean containsThirdParty(String packageName) {
        return false;
    }

    /**
     * Returns whether this category supports content that can be added or removed dynamically.
     */
    public boolean supportsWallpaperSetUpdates() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Category)) return false;
        if (obj == this) return true;
        return TextUtils.equals(getCollectionId(), ((Category) obj).getCollectionId());
    }

    @Override
    public int hashCode() {
        return mCollectionId == null ? super.hashCode() : mCollectionId.hashCode();
    }
}
