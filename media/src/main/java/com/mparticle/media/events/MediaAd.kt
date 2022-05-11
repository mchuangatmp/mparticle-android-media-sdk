package com.mparticle.media.events

import android.app.Application
import com.mparticle.MParticle
import com.mparticle.MParticleOptions
import com.mparticle.identity.BaseIdentityTask


class MediaAd(
    var title: String? = null,
    var duration: Long? = null,
    var id: String? = null,
    var advertiser: String? = null,
    var campaign: String? = null,
    var creative: String? = null,
    var placement: String? = null,
    var position: Int? = null,
    var siteId: String? = null,
    internal var adStartTimestamp: Long? = null,
    internal var adEndTimestamp: Long? = null,
    internal var adSkipped: Boolean = false,
    internal var adCompleted: Boolean = false
)