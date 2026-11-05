package com.liskovsoft.smartyoutubetv2.tv.ui.playback;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
 * Last seconds of a video: two suggested videos on the left, like the YouTube TV end screen.
 * Display only: it never takes focus, so the player keys keep working as before.
 */
public class EndScreenView extends LinearLayout {
    private static final int CARDS = 2;
    private List<Video> mVideos;
    private int mSelected = -1;
    private OnVideoClickListener mListener;

    public interface OnVideoClickListener {
        void onVideoClick(Video video);
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        mListener = listener;
    }

    public EndScreenView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setFocusable(false);
        setVisibility(GONE);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(340), LayoutParams.WRAP_CONTENT, Gravity.START | Gravity.TOP);
        lp.setMarginStart(dp(24));
        lp.topMargin = dp(70);
        setLayoutParams(lp);
    }

    public void show(List<Video> videos) {
        if (videos == null || videos.size() < CARDS) {
            hide();
            return;
        }

        if (getVisibility() != VISIBLE) {
            mVideos = videos;
            mSelected = -1;
            removeAllViews();
            for (int i = 0; i < CARDS; i++) {
                addView(createCard(videos.get(i), i));
            }
            addView(createHint());
            setAlpha(0f);
            setTranslationX(-dp(28));
            setVisibility(VISIBLE);
            animate().alpha(1f).translationX(0f).setDuration(250).start();
        }
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

    public void hide() {
        if (getVisibility() != GONE) {
            setVisibility(GONE);
            mSelected = -1;
            removeAllViews();
        }
    }

    public boolean isShown2() {
        return getVisibility() == VISIBLE;
    }

    public int getSelected() {
        return mSelected;
    }

    public Video getSelectedVideo() {
        return mSelected >= 0 && mVideos != null && mSelected < mVideos.size() ? mVideos.get(mSelected) : null;
    }

    /** White ring on the focused card (-1 clears), like the YouTube TV focus. */
    public void select(int index) {
        mSelected = index;
        for (int i = 0; i < Math.min(CARDS, getChildCount()); i++) {
            GradientDrawable border = (GradientDrawable) getChildAt(i).getBackground();
            border.setStroke(dp(i == index ? 5 : 2), i == index ? Color.WHITE : Color.argb(178, 255, 255, 255));
        }
    }

    private View createCard(Video video, int index) {
        FrameLayout card = new FrameLayout(getContext());
        LayoutParams lp = new LayoutParams(dp(340), dp(191));
        if (index > 0) {
            lp.topMargin = dp(37);
        }
        card.setLayoutParams(lp);
        card.setClickable(true); // touch or mouse; the remote uses the focus logic of the player
        card.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onVideoClick(video);
            }
        });

        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(dp(12));
        border.setStroke(dp(2), Color.argb(178, 255, 255, 255));
        border.setColor(Color.argb(200, 20, 20, 20));
        card.setBackground(border);
        card.setPadding(dp(2), dp(2), dp(2), dp(2));

        ImageView image = new ImageView(getContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.addView(image, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (video.cardImageUrl != null) {
            Glide.with(getContext()).load(video.cardImageUrl).into(image);
        }

        TextView title = new TextView(getContext());
        title.setText(video.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setShadowLayer(dp(2), 0, 0, Color.BLACK);
        title.setPadding(dp(10), dp(8), dp(10), dp(24));
        // black -> transparent gradient behind the title (endscreen_element_gradient_top of the YouTube app)
        GradientDrawable fade = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.BLACK, Color.argb(203, 0, 0, 0), Color.TRANSPARENT});
        title.setBackground(fade);
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

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
