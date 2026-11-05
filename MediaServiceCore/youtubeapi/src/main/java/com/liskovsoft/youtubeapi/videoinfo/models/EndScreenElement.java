package com.liskovsoft.youtubeapi.videoinfo.models;

import com.liskovsoft.googlecommon.common.converters.jsonpath.JsonPath;
import com.liskovsoft.googlecommon.common.models.V2.TextItem;
import com.liskovsoft.mediaserviceinterfaces.data.EndScreenItem;
import com.liskovsoft.sharedutils.helpers.Helpers;

/**
 * player response: endscreen.endscreenRenderer.elements[].endscreenElementRenderer
 */
public class EndScreenElement implements EndScreenItem {
    @JsonPath("$.style")
    private String mStyle;

    @JsonPath("$.title")
    private TextItem mTitle;

    @JsonPath("$.image.thumbnails[0].url")
    private String mThumbnailUrl;

    @JsonPath("$.videoDuration")
    private TextItem mDuration;

    @JsonPath("$.endpoint.watchEndpoint.videoId")
    private String mVideoId;

    @JsonPath("$.startMs")
    private String mStartMs;

    @JsonPath("$.endMs")
    private String mEndMs;

    public boolean isVideo() {
        return "VIDEO".equals(mStyle) && mVideoId != null;
    }

    @Override
    public String getTitle() {
        return mTitle != null ? mTitle.toString() : null;
    }

    @Override
    public String getVideoId() {
        return mVideoId;
    }

    @Override
    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    @Override
    public String getDurationText() {
        return mDuration != null ? mDuration.toString() : null;
    }

    @Override
    public long getStartMs() {
        return Helpers.parseLong(mStartMs);
    }

    @Override
    public long getEndMs() {
        return Helpers.parseLong(mEndMs);
    }
}
