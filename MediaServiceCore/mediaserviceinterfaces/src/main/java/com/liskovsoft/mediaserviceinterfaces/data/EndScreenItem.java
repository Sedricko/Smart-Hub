package com.liskovsoft.mediaserviceinterfaces.data;

/**
 * A card the channel owner put on the last seconds of a video (YouTube "endscreen").
 */
public interface EndScreenItem {
    String getTitle();
    String getVideoId();
    String getThumbnailUrl();
    String getDurationText();
    long getStartMs();
    long getEndMs();
}
