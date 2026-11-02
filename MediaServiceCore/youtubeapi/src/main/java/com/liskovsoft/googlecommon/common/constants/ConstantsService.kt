package com.liskovsoft.googlecommon.common.constants

import com.liskovsoft.googlecommon.common.constants.data.ConstantsResult
import com.liskovsoft.youtubeapi.BuildConfig

/**
 * Google API credentials are baked in at build time (env vars or gradle properties named
 * GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, YOUTUBE_DATA_API_KEY). Nothing is downloaded from a third party.
 * Blank values mean "not configured": features that need them (channel metadata) simply return no data.
 */
internal object ConstantsService {
    @JvmStatic
    val constants: ConstantsResult? by lazy {
        val id = BuildConfig.GOOGLE_CLIENT_ID.ifBlank { null }
        val secret = BuildConfig.GOOGLE_CLIENT_SECRET.ifBlank { null }
        val key = BuildConfig.YOUTUBE_DATA_API_KEY.ifBlank { null }

        if (id == null && secret == null && key == null) null else ConstantsResult(id, secret, key)
    }
}
