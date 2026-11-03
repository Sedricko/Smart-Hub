package com.liskovsoft.smartyoutubetv2.tv.ui.browse;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.PageRow;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * Phone mode, laid out like the YouTube mobile app (21.44): five-ish main tabs in a bottom bar
 * (48dp, 24dp icons, 10sp labels, equal weight) and every other section as a pill chip row under the top bar.
 * Measures come from that app's resources; colors stay Smart Hub's.
 */
public class PhoneNavBar {
    public interface OnSelect {
        void onSelect(int index);
    }

    private static final int[] MAIN_TABS = {
            MediaGroup.TYPE_HOME, MediaGroup.TYPE_SHORTS, MediaGroup.TYPE_SUBSCRIPTIONS,
            MediaGroup.TYPE_HISTORY, MediaGroup.TYPE_SETTINGS};

    private final HorizontalScrollView mBar;
    private final LinearLayout mBarItems;
    private final HorizontalScrollView mChips;
    private final LinearLayout mChipItems;
    private final ArrayObjectAdapter mSections;
    private final OnSelect mListener;
    private final Runnable mOnSearch;
    private final int mBg;
    private final int mAccent;
    private final int mIdle;
    private final int mChipText;
    private final float mD;
    private int mSelectedId = -1;

    public static boolean isPhone(Context context) {
        UiModeManager uiMode = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiMode == null || uiMode.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private static boolean isMain(int id) {
        for (int main : MAIN_TABS) {
            if (main == id) {
                return true;
            }
        }
        return false;
    }

    public PhoneNavBar(ViewGroup root, ArrayObjectAdapter sections, int bgColor, int accent, OnSelect listener, Runnable onSearch) {
        Context context = root.getContext();
        mD = context.getResources().getDisplayMetrics().density;
        mSections = sections;
        mListener = listener;
        mOnSearch = onSearch;
        mBg = bgColor;
        mAccent = accent;
        boolean lightBg = ColorUtils.calculateLuminance(bgColor) > 0.5;
        mIdle = lightBg ? 0xFF5F6B7A : 0xFFAAB4C3;
        mChipText = lightBg ? 0xFF0F172A : 0xFFF1F5F9;

        mBarItems = new LinearLayout(context);
        mBar = new HorizontalScrollView(context);
        mBar.setFillViewport(true);
        mBar.setHorizontalScrollBarEnabled(false);
        mBar.setBackgroundColor(bgColor);
        mBar.addView(mBarItems, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(mBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        mBar.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        mChipItems = new LinearLayout(context);
        mChipItems.setPadding((int) (12 * mD), 0, (int) (12 * mD), 0);
        mChips = new HorizontalScrollView(context);
        mChips.setHorizontalScrollBarEnabled(false);
        mChips.addView(mChipItems);
        FrameLayout.LayoutParams chipsLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (40 * mD), Gravity.TOP);
        chipsLp.topMargin = (int) (66 * mD);
        root.addView(mChips, chipsLp);
        // Sits right under the title row (logo and profile), below the status bar
        mChips.setOnApplyWindowInsetsListener((v, insets) -> {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
            int top = insets.getSystemWindowInsetTop() + (int) (66 * mD);
            if (lp.topMargin != top) {
                lp.topMargin = top;
                v.setLayoutParams(lp);
            }
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
        mBar.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> listener.onSelect(b - t));
    }

    public void select(int headerId) {
        mSelectedId = headerId;
        rebuild();
    }

    private void rebuild() {
        Context context = mBar.getContext();
        mBarItems.removeAllViews();
        mChipItems.removeAllViews();

        addTab(context, androidx.leanback.R.drawable.lb_ic_in_app_search, context.getString(R.string.phone_search), mIdle, v -> mOnSearch.run());

        for (int i = 0; i < mSections.size(); i++) {
            SectionHeaderItem header = (SectionHeaderItem) ((PageRow) mSections.get(i)).getHeaderItem();
            boolean active = header.getId() == mSelectedId;
            final int index = i;
            View.OnClickListener click = v -> {
                Log.d("PhoneNavBar", "tap section index=" + index + " id=" + header.getId() + " name=" + header.getName());
                mListener.onSelect(index);
            };

            if (isMain((int) header.getId())) {
                addTab(context, header.getResId(), header.getName(), active ? mAccent : mIdle, click);
            } else {
                addChip(context, header.getName(), active, click);
            }
        }
    }

    // YouTube pivot bar: weight 1, 24dp icon, 10sp label
    private void addTab(Context context, int iconRes, CharSequence name, int color, View.OnClickListener click) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(0, (int) (6 * mD), 0, (int) (6 * mD));
        cell.setOnClickListener(click);
        cell.setContentDescription(name);

        if (iconRes > 0) {
            ImageView icon = new ImageView(context);
            icon.setImageResource(iconRes);
            icon.setColorFilter(color);
            cell.addView(icon, new LinearLayout.LayoutParams((int) (24 * mD), (int) (24 * mD)));
        }

        TextView label = new TextView(context);
        label.setText(name);
        label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 10);
        label.setSingleLine();
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setTextColor(color);
        label.setGravity(Gravity.CENTER);
        cell.addView(label);

        mBarItems.addView(cell, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    // YouTube chip: 32dp pill, selected one inverted
    private void addChip(Context context, CharSequence name, boolean active, View.OnClickListener click) {
        TextView chip = new TextView(context);
        chip.setText(name);
        chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 14);
        chip.setSingleLine();
        chip.setGravity(Gravity.CENTER);
        chip.setPadding((int) (12 * mD), 0, (int) (12 * mD), 0);
        chip.setTextColor(active ? (ColorUtils.calculateLuminance(mAccent) > 0.5 ? 0xFF0F172A : 0xFFFFFFFF) : mChipText);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16 * mD);
        bg.setColor(active ? mAccent : mBg);
        chip.setBackground(bg);
        chip.setOnClickListener(click);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (32 * mD));
        lp.setMarginEnd((int) (8 * mD));
        lp.gravity = Gravity.CENTER_VERTICAL;
        mChipItems.addView(chip, lp);
    }
}
