package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup;
import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup.Item;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.menu.providers.channelgroup.ChannelGroupServiceWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * "Subscriptions" without an account: the channels are kept on the device as an ordinary channel group
 * (the same store used by the channel groups feature, so they are included in backups).
 */
public final class LocalSubscriptions {
    public static final String GROUP_TITLE = "Inscrições locais";

    private LocalSubscriptions() {
    }

    public static boolean contains(Context context, String channelId) {
        ItemGroup group = find(context);
        return group != null && channelId != null && group.contains(channelId);
    }

    /**
     * @return true if the channel is saved after the call, false if it was removed
     */
    public static boolean toggle(Context context, Video video) {
        if (video == null || video.channelId == null) {
            return false;
        }

        ChannelGroupServiceWrapper service = ChannelGroupServiceWrapper.instance(context);
        ItemGroup group = find(context);

        if (group == null) {
            group = service.createChannelGroup(GROUP_TITLE, null, new ArrayList<>());
        }

        boolean subscribed;

        if (group.contains(video.channelId)) {
            group.remove(video.channelId);
            subscribed = false;
        } else {
            String title = video.isChannel() ? video.title : video.author;
            String icon = video.isChannel() ? video.cardImageUrl : null;
            group.add(service.createChannel(title != null ? title : video.channelId, icon, video.channelId));
            subscribed = true;
        }

        service.addChannelGroup(group); // persists
        return subscribed;
    }

    public static String[] channelIds(Context context) {
        ItemGroup group = find(context);
        List<String> ids = new ArrayList<>();

        if (group != null) {
            for (Item item : group.getItems()) {
                if (item.getChannelId() != null) {
                    ids.add(item.getChannelId());
                }
            }
        }

        return ids.toArray(new String[0]);
    }

    /**
     * Channels as cards for the "Channels" section
     */
    public static List<Video> channels(Context context) {
        ItemGroup group = find(context);
        List<Video> result = new ArrayList<>();

        if (group != null) {
            for (Item item : group.getItems()) {
                if (item.getChannelId() == null) {
                    continue;
                }

                Video video = new Video();
                video.channelId = item.getChannelId();
                video.title = item.getTitle();
                video.cardImageUrl = item.getIconUrl();
                video.isSubscribed = true;
                result.add(video);
            }
        }

        return result;
    }

    private static ItemGroup find(Context context) {
        return ChannelGroupServiceWrapper.instance(context).findChannelGroupByTitle(GROUP_TITLE);
    }
}
