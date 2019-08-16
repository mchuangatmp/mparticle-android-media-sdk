package com.mparticle

import com.mparticle.events.MediaAd
import com.mparticle.events.MediaAdBreak
import com.mparticle.events.MediaQoS
import com.mparticle.events.MediaSegment

class MediaSession private constructor(builder: Builder) {

    fun logMediaStart() {}

    fun logMediaStop() {}

    fun logPlay() {}

    fun logPause() {}

    fun logSeekStart(position: Long) {}

    fun logSeekEnd(position: Long) {}

    fun logBufferStart(duration: Long, bufferPercent: Double, position: Int) {}

    fun logBufferEnd(duration: Long, bufferPercent: Double, position: Int) { }

    fun logAdBreakStart(adBreak: MediaAdBreak) { }

    fun logAdBreakEnd() {}

    fun logAdStart(ad: MediaAd) {}

    fun logAdEnd() {}

    fun logAdSkip() {}

    fun logSegmentStart(segment: MediaSegment) {}

    fun logSegmentSkip() {}

    fun logSegmentEnd() {}

    fun logPlayheadPosition(playheadPosition: Long) {}

    fun logQos(qos: MediaQoS) {}

    companion object {
        @JvmStatic
        fun builder(mparticle: MParticle? = null): Builder {
            return Builder(mparticle)
        }

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        fun builder(mparticle: MParticle? = null, builderFunction: Builder.() -> Unit): MediaSession {
            var builder = Builder(mparticle)
            builder.builderFunction()
            return builder.build()
        }
    }

    class Builder internal constructor(var mparticle: MParticle? = null) {
        var title: String? = null
        var mediaContentId: String? = null
        var duration: Long? = null
        var streamType: String? = null
        var mediaType: String? = null

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun mediaId(mediaContentId: String): Builder {
            this.mediaContentId = mediaContentId
            return this
        }

        fun duration(duration: Long): Builder {
            this.duration = duration
            return this
        }

        fun streamType(streamType: String): Builder {
            this.streamType = streamType
            return this
        }

        fun mediaType(mediaType: String): Builder {
            this.mediaType = mediaType
            return this
        }

        fun build(): MediaSession {
            return MediaSession(this)
        }
    }
}