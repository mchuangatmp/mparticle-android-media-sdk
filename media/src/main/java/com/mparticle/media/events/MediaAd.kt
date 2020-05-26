package com.mparticle.media.events
import java.util.*

class MediaAd(
    var title: String? = null,
    var duration: Long? = null,
    var id: String? = null,
    var advertiser: String? = null,
    var campaign: String? = null,
    var creative: String? = null,
    var placement: Int? = null,
    var siteId: String? = null,
    var adStartTimestamp: Long? = null,
    var adEndTimestamp: Long? = null,
    var adSkipped: Boolean = false,
    var adCompleted: Boolean = false
)