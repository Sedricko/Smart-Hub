package com.liskovsoft.smartyoutubetv2.tv.ui.playback;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.List;

/**
 * Last seconds of a video: two suggested videos on the left, like the YouTube TV end screen.
 * Display only: it never takes focus, so the player keys keep working as before.
 */
public class EndScreenView extends LinearLayout {
    private static final int CARDS = 2;

    public EndScreenView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setFocusable(false);
        setVisibility(GONE);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(316), LayoutParams.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL);
        lp.setMarginStart(dp(72));
        setLayoutParams(lp);
    }

    public void show(List<Video> videos) {
        if (videos == null || videos.size() < CARDS) {
            hide();
            return;
        }

        if (getVisibility() != VISIBLE) {
            removeAllViews();
            for (int i = 0; i < CARDS; i++) {
                addView(createCard(videos.get(i), i));
            }
            setVisibility(VISIBLE);
        }
    }

    public void hide() {
        if (getVisibility() != GONE) {
            setVisibility(GONE);
            removeAllViews();
        }
    }

    private View createCard(Video video, int index) {
        FrameLayout card = new FrameLayout(getContext());
        LayoutParams lp = new LayoutParams(dp(316), dp(178));
        if (index > 0) {
            lp.topMargin = dp(36);
        }
        card.setLayoutParams(lp);

        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(dp(12));
        border.setStroke(dp(2), Color.argb(170, 200, 200, 200));
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
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setMaxLines(3);
        title.setShadowLayer(dp(2), 0, 0, Color.BLACK);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        tlp.setMargins(dp(12), dp(10), dp(12), 0);
        card.addView(title, tlp);

        if (video.badge != null) {
            TextView time = new TextView(getContext());
            time.setText(video.badge);
            time.setTextColor(Color.WHITE);
            time.setTypeface(Typeface.DEFAULT_BOLD);
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            time.setPadding(dp(6), dp(2), dp(6), dp(2));
            time.setBackgroundColor(Color.argb(190, 0, 0, 0));
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
