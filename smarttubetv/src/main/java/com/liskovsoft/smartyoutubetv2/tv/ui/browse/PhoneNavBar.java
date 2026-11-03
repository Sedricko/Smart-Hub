package com.liskovsoft.smartyoutubetv2.tv.ui.browse;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import com.liskovsoft.smartyoutubetv2.tv.R;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.PageRow;

/**
 * Phone mode: sections live in a bottom bar instead of the TV sidebar.
 */
public class PhoneNavBar {
    public interface OnSelect {
        void onSelect(int index);
    }

    private final HorizontalScrollView mScroll;
    private final LinearLayout mItems;
    private final ArrayObjectAdapter mSections;
    private final OnSelect mListener;
    private final Runnable mOnSearch;
    private final int mBg;
    private final int mActive;
    private final int mIdle;
    private int mSelectedId = -1;

    public static boolean isPhone(Context context) {
        UiModeManager uiMode = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiMode == null || uiMode.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public PhoneNavBar(ViewGroup root, ArrayObjectAdapter sections, int bgColor, int accent, OnSelect listener, Runnable onSearch) {
        Context context = root.getContext();
        mSections = sections;
        mListener = listener;
        mOnSearch = onSearch;
        mBg = bgColor;
        mActive = accent;
        mIdle = ColorUtils.calculateLuminance(bgColor) > 0.5 ? 0xFF5F6B7A : 0xFFAAB4C3;

        mItems = new LinearLayout(context);
        mItems.setOrientation(LinearLayout.HORIZONTAL);
        mScroll = new HorizontalScrollView(context);
        mScroll.setHorizontalScrollBarEnabled(false);
        mScroll.setBackgroundColor(bgColor);
        mScroll.addView(mItems);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        root.addView(mScroll, lp);

        mScroll.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        sections.registerObserver(new ObjectAdapter.DataObserver() {
            @Override
            public void onChanged() {
                rebuild();
            }
        });
        rebuild();
    }

    public void setOnHeightChanged(OnSelect listener) {
        mScroll.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> listener.onSelect(b - t));
    }

    public void select(int headerId) {
        mSelectedId = headerId;
        rebuild();
    }

    private void rebuild() {
        Context context = mScroll.getContext();
        float d = context.getResources().getDisplayMetrics().density;
        mItems.removeAllViews();
        addCell(context, d, androidx.leanback.R.drawable.lb_ic_in_app_search, context.getString(R.string.phone_search), mIdle, v -> mOnSearch.run());

        for (int i = 0; i < mSections.size(); i++) {
            SectionHeaderItem header = (SectionHeaderItem) ((PageRow) mSections.get(i)).getHeaderItem();
            boolean active = header.getId() == mSelectedId;
            int color = active ? mActive : mIdle;
            final int index = i;

            addCell(context, d, header.getResId(), header.getName(), color, v -> mListener.onSelect(index));
        }
    }

    private void addCell(Context context, float d, int iconRes, CharSequence name, int color, View.OnClickListener click) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding((int) (4 * d), (int) (8 * d), (int) (4 * d), (int) (8 * d));
        cell.setOnClickListener(click);
        cell.setContentDescription(name);

        if (iconRes > 0) {
            ImageView icon = new ImageView(context);
            icon.setImageResource(iconRes);
            icon.setColorFilter(color);
            cell.addView(icon, new LinearLayout.LayoutParams((int) (24 * d), (int) (24 * d)));
        }

        TextView label = new TextView(context);
        label.setText(name);
        label.setTextSize(11);
        label.setSingleLine();
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setTextColor(color);
        label.setGravity(Gravity.CENTER);
        cell.addView(label);

        // Fixed width: LinearLayout ignores minimum width, and names must not be clipped
        mItems.addView(cell, new LinearLayout.LayoutParams((int) (88 * d), ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
