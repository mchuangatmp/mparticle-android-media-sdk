package com.mparticle.media.events

class MediaSegment(
    var title: String? = null,
    var index: Int? = null,
    var duration: Long? = null,
    var segmentStartTimestamp: Long? = null,
    var segmentEndTimestamp: Long? = null,
    var segmentSkipped: Boolean = false,
    var segmentCompleted: Boolean = false
)