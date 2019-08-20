package com.mparticle

import com.mparticle.events.MediaAd
import com.mparticle.events.MediaAdBreak
import com.mparticle.events.MediaQoS
import com.mparticle.events.MediaSegment
import com.mparticle.events.MediaEvent
import com.mparticle.events.MediaEventType
import com.mparticle.internal.Logger
import com.mparticle.internal.MPUtility
import java.util.*

class MediaSession protected constructor(builder: Builder) {
    var sessionId: String = ""
        private set
    var mparticleInstance: MParticle?

    var title: String
        private set
    var mediaContentId: String
        private set
    var duration: Long?
        private set
    var contentType: String
        private set
    var streamType: String
        private set

    var currentPlayheadPosition: Long = 0
        private set

    init {
        if (builder.mparticle == null) {
            builder.mparticle = MParticle.getInstance()
            if (builder.mparticle == null) {
                Logger.error("MParticle must be started in order to build a MediaSession")
            }
        }
        mparticleInstance = builder.mparticle
        title = builder.title.require("title")
        mediaContentId = builder.mediaContentId.require("mediaContentId")
        duration = builder.duration
        contentType = builder.contentType.require("contentType")
        streamType = builder.streamType.require("streamType")
    }

    fun logMediaSessionStart() {
        sessionId = UUID.randomUUID().toString()
        val mediaSessionEvent = MediaEvent(MediaEventType.SessionStart, this)
        logEvent(mediaSessionEvent)
    }

    fun logMediaSessionEnd() {
        val mediaSessionEvent = MediaEvent(MediaEventType.SessionEnd, this)
        logEvent(mediaSessionEvent)
    }

    fun logMediaContentEnd() {
        val mediaSessionEvent = MediaEvent(MediaEventType.ContentEnd, this)
        logEvent(mediaSessionEvent)
    }

    fun logPlay() {
        val playEvent = MediaEvent(MediaEventType.Play, this)
        logEvent(playEvent)
    }

    fun logPause() {
        val pauseEvent = MediaEvent(MediaEventType.Pause, this)
        logEvent(pauseEvent)
    }

    fun logSeekStart(position: Long) {
        val seekStartEvent = MediaEvent(MediaEventType.SeekStart, this).apply {
            this.seekPosition = position
        }
        logEvent(seekStartEvent)
    }

    fun logSeekEnd(position: Long) {
        val seekEndEvent = MediaEvent(MediaEventType.SeekEnd, this).apply {
            this.seekPosition = position
        }
        logEvent(seekEndEvent)
    }

    fun logBufferStart(duration: Long, bufferPercent: Double, position: Long) {
        val bufferStart = MediaEvent(MediaEventType.BufferStart, this).apply {
            this.bufferDuration = duration
            this.bufferPosition = position
            this.bufferPercent = bufferPercent
        }
        logEvent(bufferStart)
    }

    fun logBufferEnd(duration: Long, bufferPercent: Double, position: Long) {
        val bufferEnd = MediaEvent(MediaEventType.BufferEnd, this).apply {
            this.bufferDuration = duration
            this.bufferPercent = bufferPercent
            this.bufferPosition = position
        }
        logEvent(bufferEnd)
    }

    @JvmSynthetic
    fun logAdBreakStart(builder: MediaAdBreak.() -> Unit) {
        val mediaAdBreak = MediaAdBreak()
        mediaAdBreak.builder()
        logAdBreakStart(mediaAdBreak)
    }

    fun logAdBreakStart(adBreak: MediaAdBreak) {
        val adBreakEvent = MediaEvent(MediaEventType.AdBreakStart, this).apply {
            this.adBreak = adBreak
        }
        logEvent(adBreakEvent)
    }

    fun logAdBreakEnd() {
        val adBreakEvent = MediaEvent(MediaEventType.AdBreakEnd, this)
        logEvent(adBreakEvent)
    }

    @JvmSynthetic
    fun logAdStart(builder: MediaAd.() -> Unit) {
        val mediaAd = MediaAd()
        mediaAd.builder()
        logAdStart(mediaAd)
    }

    fun logAdStart(ad: MediaAd) {
        val adStartEvent = MediaEvent(MediaEventType.AdStart, this).apply {
            mediaAd = ad
        }
        logEvent(adStartEvent)
    }

    fun logAdEnd() {
        val adEndEvent = MediaEvent(MediaEventType.AdEnd, this)
        logEvent(adEndEvent)
    }

    fun logAdSkip() {
        val adSkipEvent = MediaEvent(MediaEventType.AdSkip, this)
        logEvent(adSkipEvent)
    }

    @JvmSynthetic
    fun logSegmentStart(builder: MediaSegment.() -> Unit) {
        val mediaSegment = MediaSegment()
        mediaSegment.builder()
        logSegmentStart(mediaSegment)
    }

    fun logSegmentStart(segment: MediaSegment) {
        val segmentStartEvent = MediaEvent(MediaEventType.SegmentStart, this).apply {
            this.segment = segment
        }
        logEvent(segmentStartEvent)
    }

    fun logSegmentSkip() {
        val segmentSkipEvent = MediaEvent(MediaEventType.SegmentSkip, this)
        logEvent(segmentSkipEvent)
    }

    fun logSegmentEnd() {
        val segmentEndEvent = MediaEvent(MediaEventType.SegmentEnd, this)
        logEvent(segmentEndEvent)
    }

    fun logPlayheadPosition(playheadPosition: Long) {
        currentPlayheadPosition = playheadPosition
        val playheadEvent = MediaEvent(MediaEventType.UpdatePlayheadPosition, this).apply {
            this.playheadPosition = playheadPosition
        }
        logEvent(playheadEvent)
    }

    @JvmSynthetic
    fun logQos(builder: MediaQoS.() -> Unit) {
        val mediaQos = MediaQoS()
        mediaQos.builder()
        logQos(mediaQos)
    }

    fun logQos(qos: MediaQoS) {
        val qosEvent = MediaEvent(MediaEventType.UpdateQoS, this).apply {
            this.qos = qos
        }
        logEvent(qosEvent)
    }

    protected fun logEvent(mediaEvent: MediaEvent) {
        if (mparticleInstance == null) {
            mparticleInstance = MParticle.getInstance()
        }
        mparticleInstance?.logEvent(mediaEvent) ?: Logger.error("MParticle instance is null, unable to log MediaEvent")
    }

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
        var contentType: String? = null

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun mediaContentId(mediaContentId: String): Builder {
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

        fun contentType(contentType: String): Builder {
            this.contentType = contentType
            return this
        }

        fun build(): MediaSession {
            return MediaSession(this)
        }
    }

}

private fun String?.require(variableName: String): String {
    if (this == null) {
        val message = "\"$variableName\" must not be null"
        if (MPUtility.isDevEnv()) {
            throw RuntimeException(message)
        } else {
            Logger.error(message)
        }
    }
    return this ?: ""
}