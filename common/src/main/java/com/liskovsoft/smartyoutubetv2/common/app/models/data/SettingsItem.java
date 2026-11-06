package com.liskovsoft.smartyoutubetv2.common.app.models.data;

public class SettingsItem {
    /** Settings are shown in these groups, in this order. */
    public static final int CATEGORY_ACCOUNT = 1;
    public static final int CATEGORY_PLAYBACK = 2;
    public static final int CATEGORY_APP = 3;
    public static final int CATEGORY_SYSTEM = 4;
    public static final int CATEGORY_OTHER = 99;

    public final String title;
    public final Runnable onClick;
    public int imageResId;
    public final int category;

    public SettingsItem(String title, Runnable onClick) {
        this(title, onClick, -1);
    }

    public SettingsItem(String title, Runnable onClick, int imageResId) {
        this(title, onClick, imageResId, CATEGORY_OTHER);
    }

    public SettingsItem(String title, Runnable onClick, int imageResId, int category) {
        this.title = title;
        this.onClick = onClick;
        this.imageResId = imageResId;
        this.category = category;
    }
}
