package com.liskovsoft.youtubeapi.videoinfo.models;

import com.liskovsoft.googlecommon.common.converters.jsonpath.JsonPath;
import com.liskovsoft.googlecommon.common.models.V2.TextItem;
import com.liskovsoft.mediaserviceinterfaces.data.EndScreenItem;
import com.liskovsoft.sharedutils.helpers.Helpers;

/**
 * Info card the channel owner shows at any moment of the video (when he mentions another video).
 * player response: cards.cardCollectionRenderer.cards[].cardRenderer
 */
public class InfoCardElement implements EndScreenItem {
    private static final String CONTENT = "$.content.videoInfoCardContentRenderer";

    @JsonPath(CONTENT + ".videoTitle")
    private TextItem mTitle;

    @JsonPath(CONTENT + ".videoThumbnail.thumbnails[0].url")
    private String mThumbnailUrl;

    @JsonPath(CONTENT + ".lengthString")
    private TextItem mDuration;

    @JsonPath(CONTENT + ".action.watchEndpoint.videoId")
    private String mVideoId;

    @JsonPath("$.cueRanges[0].startCardActiveMs")
    private String mStartMs;

    @JsonPath("$.cueRanges[0].endCardActiveMs")
    private String mEndMs;

    public boolean isVideo() {
        return mVideoId != null;
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
