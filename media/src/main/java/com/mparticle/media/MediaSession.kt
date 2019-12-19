package com.mparticle.media

import com.mparticle.MPEvent
import com.mparticle.MParticle
import com.mparticle.media.events.*
import com.mparticle.media.internal.Logger
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
        internal set

    var attributes: MutableMap<String, String> = mutableMapOf()
        get() = MediaEvent(this).getSessionAttributes()
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

    fun logMediaSessionStart(options: Options? = null) {
        sessionId = UUID.randomUUID().toString()
        val mediaSessionEvent = MediaEvent(this, MediaEventName.SESSION_START, options = options)
        logEvent(mediaSessionEvent)
    }

    fun logMediaSessionEnd(options: Options? = null) {
        val mediaSessionEvent = MediaEvent(this, MediaEventName.SESSION_END, options = options)
        logEvent(mediaSessionEvent)
    }

    fun logMediaContentEnd(options: Options? = null) {
        val mediaSessionEvent = MediaEvent(this, MediaEventName.CONTENT_END, options = options)
        logEvent(mediaSessionEvent)
    }

    fun logPlay(options: Options? = null) {
        val playEvent = MediaEvent(this, MediaEventName.PLAY, options = options)
        logEvent(playEvent)
    }

    fun logPause(options: Options? = null) {
        val pauseEvent = MediaEvent(this, MediaEventName.PAUSE, options = options)
        logEvent(pauseEvent)
    }

    fun logSeekStart(position: Long, options: Options? = null) {
        val seekStartEvent = MediaEvent(this, MediaEventName.SEEK_START, options = options).apply {
            this.seekPosition = position
        }
        logEvent(seekStartEvent)
    }

    fun logSeekEnd(position: Long, options: Options? = null) {
        val seekEndEvent = MediaEvent(this, MediaEventName.SEEK_END, options = options).apply {
            this.seekPosition = position
        }
        logEvent(seekEndEvent)
    }

    fun logBufferStart(duration: Long, bufferPercent: Double, position: Long, options: Options? = null) {
        val bufferStart = MediaEvent(this, MediaEventName.BUFFER_START, options = options).apply {
            this.bufferDuration = duration
            this.bufferPosition = position
            this.bufferPercent = bufferPercent
        }
        logEvent(bufferStart)
    }

    fun logBufferEnd(duration: Long, bufferPercent: Double, position: Long, options: Options? = null) {
        val bufferEnd = MediaEvent(this, MediaEventName.BUFFER_END, options = options).apply {
            this.bufferDuration = duration
            this.bufferPercent = bufferPercent
            this.bufferPosition = position
        }
        logEvent(bufferEnd)
    }

    @JvmSynthetic
    fun logAdBreakStart(builder: MediaAdBreak.() -> Unit) {
        logAdBreakStart(null, builder)
    }

    @JvmSynthetic
    fun logAdBreakStart(options: Options? = null, builder: MediaAdBreak.()-> Unit) {
        val mediaAdBreak = MediaAdBreak()
        mediaAdBreak.builder()
        logAdBreakStart(mediaAdBreak, options)
    }

    fun logAdBreakStart(adBreak: MediaAdBreak, options: Options? = null) {
        val adBreakEvent = MediaEvent(this, MediaEventName.AD_BREAK_START, options = options).apply {
            this.adBreak = adBreak
        }
        logEvent(adBreakEvent)
    }

    fun logAdBreakEnd(options: Options? = null) {
        val adBreakEvent = MediaEvent(this, MediaEventName.AD_BREAK_END, options = options)
        logEvent(adBreakEvent)
    }

    @JvmSynthetic
    fun logAdStart(builder: MediaAd.() -> Unit) {
        logAdStart(null, builder)
    }

    fun logAdStart(options: Options? = null, builder: MediaAd.() -> Unit) {
        val mediaAd = MediaAd()
        mediaAd.builder()
        logAdStart(mediaAd, options)
    }

    fun logAdStart(ad: MediaAd, options: Options? = null) {
        val adStartEvent = MediaEvent(this, MediaEventName.AD_START, options = options).apply {
            mediaAd = ad
        }
        logEvent(adStartEvent)
    }

    fun logAdEnd(options: Options? = null) {
        val adEndEvent = MediaEvent(this, MediaEventName.AD_END, options = options)
        logEvent(adEndEvent)
    }

    fun logAdSkip(options: Options? = null) {
        val adSkipEvent = MediaEvent(this, MediaEventName.AD_SKIP, options = options)
        logEvent(adSkipEvent)
    }

    @JvmSynthetic
    fun logSegmentStart(builder: MediaSegment.() -> Unit) {
        logSegmentStart(null, builder)
    }

    fun logSegmentStart(options: Options? = null, builder: MediaSegment.() -> Unit) {
        val mediaSegment = MediaSegment()
        mediaSegment.builder()
        logSegmentStart(mediaSegment, options)
    }

    fun logSegmentStart(segment: MediaSegment, options: Options? = null) {
        val segmentStartEvent = MediaEvent(this, MediaEventName.SEGMENT_START, options = options).apply {
            this.segment = segment
        }
        logEvent(segmentStartEvent)
    }

    fun logSegmentSkip(options: Options? = null) {
        val segmentSkipEvent = MediaEvent(this, MediaEventName.SEGMENT_SKIP, options = options)
        logEvent(segmentSkipEvent)
    }

    fun logSegmentEnd(options: Options? = null) {
        val segmentEndEvent = MediaEvent(this, MediaEventName.SEGMENT_END, options = options)
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
        logQos(null, builder)
    }

    fun logQos(options: Options? = null, builder: MediaQoS.() -> Unit) {
        val mediaQos = MediaQoS()
        mediaQos.builder()
        logQos(mediaQos, options)
    }

    fun logQos(qos: MediaQoS, options: Options? = null) {
        val qosEvent = MediaEvent(this, MediaEventName.UPDATE_QOS, options = options).apply {
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
        Logger.error("\"$variableName\" should not be null")
    }
    return this ?: ""
}

interface MediaEventListener {
    fun onLogMediaEvent(mediaEvent: MediaEvent)
}