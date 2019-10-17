package com.mparticle

import com.mparticle.events.*
import com.mparticle.internal.Logger
import com.mparticle.internal.MPUtility
import java.util.*

class MediaSession protected constructor(builder: Builder) {
    var sessionId: String? = null
        private set
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
    var currentPlayheadPosition: Long? = null
        private set

    var attributes: MutableMap<String, String> = MediaEvent(this).getSessionAttributes()
        private set

    private var mparticleInstance: MParticle?
    private var logMPEvents: Boolean
    private var logMediaEvents: Boolean

    @JvmSynthetic
    var mediaEventListener: ((MediaEvent) -> Unit)? = null

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
        logMPEvents = builder.logMPEvents
        logMediaEvents = builder.logMediaEvents
    }

    fun logMediaSessionStart() {
        sessionId = UUID.randomUUID().toString()
        val mediaSessionEvent = MediaEvent(this, MediaEventName.SESSION_START)
        logEvent(mediaSessionEvent)
    }

    fun logMediaSessionEnd() {
        val mediaSessionEvent = MediaEvent(this, MediaEventName.SESSION_END)
        logEvent(mediaSessionEvent)
    }

    fun logMediaContentEnd() {
        val mediaSessionEvent = MediaEvent(this, MediaEventName.CONTENT_END)
        logEvent(mediaSessionEvent)
    }

    fun logPlay() {
        val playEvent = MediaEvent(this, MediaEventName.PLAY)
        logEvent(playEvent)
    }

    fun logPause() {
        val pauseEvent = MediaEvent(this, MediaEventName.PAUSE)
        logEvent(pauseEvent)
    }

    fun logSeekStart(position: Long) {
        val seekStartEvent = MediaEvent(this, MediaEventName.SEEK_START).apply {
            this.seekPosition = position
        }
        logEvent(seekStartEvent)
    }

    fun logSeekEnd(position: Long) {
        val seekEndEvent = MediaEvent(this, MediaEventName.SEEK_END).apply {
            this.seekPosition = position
        }
        logEvent(seekEndEvent)
    }

    fun logBufferStart(duration: Long, bufferPercent: Double, position: Long) {
        val bufferStart = MediaEvent(this, MediaEventName.BUFFER_START).apply {
            this.bufferDuration = duration
            this.bufferPosition = position
            this.bufferPercent = bufferPercent
        }
        logEvent(bufferStart)
    }

    fun logBufferEnd(duration: Long, bufferPercent: Double, position: Long) {
        val bufferEnd = MediaEvent(this, MediaEventName.BUFFER_END).apply {
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
        val adBreakEvent = MediaEvent(this, MediaEventName.AD_BREAK_START).apply {
            this.adBreak = adBreak
        }
        logEvent(adBreakEvent)
    }

    fun logAdBreakEnd() {
        val adBreakEvent = MediaEvent(this, MediaEventName.AD_BREAK_END)
        logEvent(adBreakEvent)
    }

    @JvmSynthetic
    fun logAdStart(builder: MediaAd.() -> Unit) {
        val mediaAd = MediaAd()
        mediaAd.builder()
        logAdStart(mediaAd)
    }

    fun logAdStart(ad: MediaAd) {
        val adStartEvent = MediaEvent(this, MediaEventName.AD_START).apply {
            mediaAd = ad
        }
        logEvent(adStartEvent)
    }

    fun logAdEnd() {
        val adEndEvent = MediaEvent(this, MediaEventName.AD_END)
        logEvent(adEndEvent)
    }

    fun logAdSkip() {
        val adSkipEvent = MediaEvent(this, MediaEventName.AD_SKIP)
        logEvent(adSkipEvent)
    }

    @JvmSynthetic
    fun logSegmentStart(builder: MediaSegment.() -> Unit) {
        val mediaSegment = MediaSegment()
        mediaSegment.builder()
        logSegmentStart(mediaSegment)
    }

    fun logSegmentStart(segment: MediaSegment) {
        val segmentStartEvent = MediaEvent(this, MediaEventName.SEGMENT_START).apply {
            this.segment = segment
        }
        logEvent(segmentStartEvent)
    }

    fun logSegmentSkip() {
        val segmentSkipEvent = MediaEvent(this, MediaEventName.SEGMENT_SKIP)
        logEvent(segmentSkipEvent)
    }

    fun logSegmentEnd() {
        val segmentEndEvent = MediaEvent(this, MediaEventName.SEGMENT_END)
        logEvent(segmentEndEvent)
    }

    fun logPlayheadPosition(playheadPosition: Long) {
        currentPlayheadPosition = playheadPosition
        val playheadEvent = MediaEvent(this, MediaEventName.UPDATE_PLAYHEAD_POSITION).apply {
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
        val qosEvent = MediaEvent(this, MediaEventName.UPDATE_QOS).apply {
            this.qos = qos
        }
        logEvent(qosEvent)
    }

    fun buildMPEvent(eventName: String, customAttributes: Map<String, String>): MPEvent {
        val eventAttributes = attributes
        eventAttributes.putAll(customAttributes)
        return MPEvent.Builder(eventName)
            .customAttributes(customAttributes)
            .build()
    }

    fun setMediaEventListener(listener: MediaEventListener) {
        mediaEventListener = { mediaEvent -> listener.onLogMediaEvent(mediaEvent)}
    }

    protected fun logEvent(mediaEvent: MediaEvent) {
        if (mparticleInstance == null) {
            mparticleInstance = MParticle.getInstance()
        }

        mediaEventListener?.invoke(mediaEvent)

        if (logMediaEvents) {
            mparticleInstance?.logEvent(mediaEvent)
                ?: Logger.error("MParticle instance is null, unable to log MediaEvent")
        }
        if (logMPEvents) {
            //never log UPDATE_PLAYHEAD_POSITION Media Events, far to high volume to be logging to our server
            if (mediaEvent.eventName != MediaEventName.UPDATE_PLAYHEAD_POSITION) {
                val mpEvent = mediaEvent.toMPEvent()
                mparticleInstance?.logEvent(mpEvent)
            }
        }
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
            @JvmSynthetic
            set
        var mediaContentId: String? = null
            @JvmSynthetic
            set
        var duration: Long? = null
            @JvmSynthetic
            set
        var streamType: String? = null
            @JvmSynthetic
            set
        var contentType: String? = null
            @JvmSynthetic
            set
        var logMediaEvents: Boolean = true
            @JvmSynthetic
            set
        var logMPEvents: Boolean = false
            @JvmSynthetic
            set

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

        fun logMediaEvents(shouldLog: Boolean): Builder {
            this.logMediaEvents = shouldLog
            return this;
        }

        fun logMPEvents(shouldLog: Boolean): Builder {
            this.logMPEvents = shouldLog
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

interface MediaEventListener {
    fun onLogMediaEvent(mediaEvent: MediaEvent)
}