package com.liskovsoft.smartyoutubetv2.common.exoplayer.versions.selector;

import android.os.Build;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hides hardware VP9 decoders known to be broken on old Android releases (and some brands).
 * The player then falls back to another format (e.g. AVC) instead of failing on a bad decoder.
 * Software decoders are left alone on purpose.
 */
public class DenyListMediaCodecSelector implements MediaCodecSelector {
    private static final Set<String> VP9_DENY = new HashSet<>();

    static {
        String release = Build.VERSION.RELEASE == null ? "" : Build.VERSION.RELEASE;

        if (release.startsWith("6.0.1")) {
            VP9_DENY.addAll(Arrays.asList("OMX.Exynos.vp9.dec", "OMX.Intel.VideoDecoder.VP9.hwr",
                    "OMX.MTK.VIDEO.DECODER.VP9", "OMX.qcom.video.decoder.vp9"));
        }
        if (release.startsWith("6.0")) {
            VP9_DENY.addAll(Arrays.asList("OMX.MTK.VIDEO.DECODER.VP9", "OMX.Nvidia.vp9.decode"));
        }
        if (release.startsWith("5.1.1")) {
            VP9_DENY.addAll(Arrays.asList("OMX.allwinner.video.decoder.vp9", "OMX.Exynos.vp9.dec",
                    "OMX.Intel.VideoDecoder.VP9.hwr", "OMX.MTK.VIDEO.DECODER.VP9", "OMX.qcom.video.decoder.vp9"));
        }
        if (release.startsWith("5.1")) {
            VP9_DENY.addAll(Arrays.asList("OMX.Exynos.VP9.Decoder", "OMX.Intel.VideoDecoder.VP9.hwr",
                    "OMX.MTK.VIDEO.DECODER.VP9"));
        }
        if (release.startsWith("5.0")) {
            VP9_DENY.addAll(Arrays.asList("OMX.allwinner.video.decoder.vp9", "OMX.Exynos.vp9.dec",
                    "OMX.Intel.VideoDecoder.VP9.hwr", "OMX.MTK.VIDEO.DECODER.VP9"));
        }
        if ("google".equals(Build.BRAND)) {
            VP9_DENY.addAll(Arrays.asList("OMX.Nvidia.vp9.decode", "OMX.Intel.VideoDecoder.VP9.hybrid"));
        }
        if ("LGE".equals(Build.BRAND)) {
            VP9_DENY.add("OMX.qcom.video.decoder.vp9");
        }
    }

    @Override
    public List<MediaCodecInfo> getDecoderInfos(String mimeType, boolean requiresSecureDecoder, boolean requiresTunnelingDecoder)
            throws MediaCodecUtil.DecoderQueryException {
        List<MediaCodecInfo> infos = MediaCodecUtil.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder);

        if (!MimeTypes.VIDEO_VP9.equals(mimeType) || VP9_DENY.isEmpty()) {
            return infos;
        }

        List<MediaCodecInfo> result = new ArrayList<>();
        for (MediaCodecInfo info : infos) {
            if (!VP9_DENY.contains(info.name)) {
                result.add(info);
            }
        }
        return result;
    }

    @Override
    public MediaCodecInfo getPassthroughDecoderInfo() throws MediaCodecUtil.DecoderQueryException {
        return MediaCodecUtil.getPassthroughDecoderInfo();
    }
}
