package com.liskovsoft.smartyoutubetv2.tv.ui.playback;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.tv.R;

import java.util.List;

/**
 * Cards the channel owner put on the video (YouTube "end screen" / info cards), stacked on the left.
 * Pure view: which cards to show and when is decided by the caller; key handling stays in the player.
 * Measures come from the YouTube TV photos: 24dp from the left edge, top at ~13% of the height.
 */
public class EndScreenView extends LinearLayout {
    /** Most cards shown at once. */
    public static final int MAX_CARDS = 2;
    private static final int WIDTH_DP = 340;
    private static final int HEIGHT_DP = 191;
    private static final int GAP_DP = 37;
    private static final int BORDER_COLOR = Color.argb(178, 255, 255, 255);
    private static final int ENTER_MS = 250;

    private List<Video> mVideos;
    private int mCardCount;
    private int mSelected = -1;
    private OnVideoClickListener mListener;

    public interface OnVideoClickListener {
        void onVideoClick(Video video);
    }

    public EndScreenView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setFocusable(false);
        setVisibility(GONE);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(WIDTH_DP), LayoutParams.WRAP_CONTENT, Gravity.START | Gravity.TOP);
        lp.setMarginStart(dp(24));
        lp.topMargin = dp(70);
        setLayoutParams(lp);
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        mListener = listener;
    }

    /** Shows 1..MAX_CARDS cards. Does nothing when the same cards are already shown. */
    public void show(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            hide();
            return;
        }

        int count = Math.min(videos.size(), MAX_CARDS);

        if (isVisible() && sameCards(videos, count)) {
            return;
        }

        boolean wasVisible = isVisible();
        mVideos = videos;
        mCardCount = count;
        mSelected = -1;
        removeAllViews();
        for (int i = 0; i < count; i++) {
            addView(createCard(videos.get(i), i));
        }
        addView(createHint());
        setVisibility(VISIBLE);

        if (!wasVisible) { // slide in like YouTube; a swap of cards does not replay it
            setAlpha(0f);
            setTranslationX(-dp(28));
            animate().alpha(1f).translationX(0f).setDuration(ENTER_MS).start();
        }
    }

    public void hide() {
        if (!isVisible()) {
            return;
        }

        animate().cancel();
        setAlpha(1f);
        setTranslationX(0f);
        setVisibility(GONE);
        mSelected = -1;
        mCardCount = 0;
        mVideos = null;
        removeAllViews();
    }

    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    public int getSelected() {
        return mSelected;
    }

    public int getCardCount() {
        return mCardCount;
    }

    public Video getSelectedVideo() {
        return mSelected >= 0 && mVideos != null && mSelected < mCardCount ? mVideos.get(mSelected) : null;
    }

    /** White ring on the focused card (-1 clears), like the YouTube TV focus. Out of range clears. */
    public void select(int index) {
        mSelected = index >= 0 && index < mCardCount ? index : -1;
        for (int i = 0; i < mCardCount; i++) {
            GradientDrawable border = (GradientDrawable) getChildAt(i).getBackground();
            border.setStroke(dp(i == mSelected ? 5 : 2), i == mSelected ? Color.WHITE : BORDER_COLOR);
        }
    }

    private boolean sameCards(List<Video> videos, int count) {
        if (mVideos == null || mCardCount != count) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            if (!TextUtils.equals(mVideos.get(i).videoId, videos.get(i).videoId)) {
                return false;
            }
        }

        return true;
    }

    private View createCard(Video video, int index) {
        FrameLayout card = new FrameLayout(getContext());
        LayoutParams lp = new LayoutParams(dp(WIDTH_DP), dp(HEIGHT_DP));
        if (index > 0) {
            lp.topMargin = dp(GAP_DP);
        }
        card.setLayoutParams(lp);
        card.setClickable(true); // touch or mouse; the remote uses the focus logic of the player
        card.setContentDescription(video.title);
        card.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onVideoClick(video);
            }
        });

        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(dp(12));
        border.setStroke(dp(2), BORDER_COLOR);
        border.setColor(Color.argb(200, 20, 20, 20));
        card.setBackground(border);
        card.setPadding(dp(2), dp(2), dp(2), dp(2));

        ImageView image = new ImageView(getContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.addView(image, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        loadThumbnail(video.cardImageUrl, image);

        TextView title = new TextView(getContext());
        title.setText(video.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setShadowLayer(dp(2), 0, 0, Color.BLACK);
        title.setPadding(dp(10), dp(8), dp(10), dp(24));
        // black -> transparent gradient behind the title (endscreen_element_gradient_top of the YouTube app)
        title.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.BLACK, Color.argb(203, 0, 0, 0), Color.TRANSPARENT}));
        card.addView(title, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START));

        if (video.badge != null) {
            TextView time = new TextView(getContext());
            time.setText(video.badge);
            time.setTextColor(Color.WHITE);
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            time.setPadding(dp(6), dp(2), dp(6), dp(2));
            time.setBackgroundColor(Color.argb(204, 0, 0, 0));
            FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.END);
            blp.setMargins(0, 0, dp(8), dp(8));
            card.addView(time, blp);
        }

        return card;
    }

    private void loadThumbnail(String url, ImageView image) {
        Context context = getContext();

        // Glide.with(context) throws IllegalArgumentException when the activity is already destroyed
        if (url == null || (context instanceof Activity && ((Activity) context).isDestroyed())) {
            return;
        }

        Glide.with(context).load(url).override(dp(WIDTH_DP), dp(HEIGHT_DP)).into(image);
    }

    private View createHint() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams rlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(20);
        row.setLayoutParams(rlp);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(R.drawable.ic_dpad_hint);
        icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(icon, new LayoutParams(dp(32), dp(32)));

        TextView hint = new TextView(getContext());
        hint.setText(R.string.end_screen_hint);
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hint.setShadowLayer(dp(2), 0, 0, Color.BLACK);
        hint.setPadding(dp(10), 0, 0, 0);
        row.addView(hint);

        return row;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
