package com.mparticle.media

import android.annotation.SuppressLint
import com.mparticle.MPEvent
import com.mparticle.MParticle
import com.mparticle.media.events.*
import com.mparticle.media.internal.Logger
import java.util.*

//Summary Event
const val MediaSessionSummary = "Media Session Summary"
const val MediaSegmentSummary = "Media Segment Summary"
const val MediaAdSummary = "Media Ad Summary"

// Session Summary Attributes
const val mediaSessionIdKey = "media_session_id"
const val startTimestampKey = "media_session_start_time"
const val endTimestampKey = "media_session_end_time"
const val contentIdKey = "content_id"
const val contentTitleKey = "content_title"
const val mediaTimeSpentKey = "media_time_spent"
const val contentTimeSpentKey = "media_content_time_spent"
const val contentCompleteKey = "media_content_complete"
const val totalSegmentsKey = "media_session_segment_total"
const val totalAdTimeSpentKey = "media_total_ad_time_spent"
const val adTimeSpentRateKey = "media_ad_time_spent_rate"
const val totalAdsKey = "media_session_ad_total"
const val adIDsKey = "media_session_ad_objects"

// Ad Summary Attributes
const val adBreakIdKey = "ad_break_id"
const val adContentIdKey = "ad_content_id"
const val adContentStartTimestampKey = "ad_content_start_time"
const val adContentEndTimestampKey = "ad_content_end_time"
const val adContentTitleKey = "ad_content_title"
const val adContentSkippedKey = "ad_skipped"
const val adContentCompletedKey = "ad_completed"

// Segment Summary Attributes
const val segmentIndexKey = "segment_index"
const val segmentTitleKey = "segment_title"
const val segmentStartTimestampKey = "segment_start_time"
const val segmentEndTimestampKey = "segment_end_time"
const val segmentTimeSpentKey = "media_segment_time_spent"
const val segmentSkippedKey = "segment_skipped"
const val segmentCompletedKey = "segment_completed"

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
    var sessionQoS = MediaQoS()

    var attributes: MutableMap<String, Any?> = mutableMapOf()
        get() = MediaEvent(this).getSessionAttributes()
        private set

    private var mparticleInstance: MParticle?
    private var logMPEvents: Boolean
    private var logMediaEvents: Boolean

    private var adContent: MediaAd? = null
    private var segment: MediaSegment? = null

    private var mediaSessionStartTimestamp: Long //Timestamp created on logMediaSessionStart event
    private var mediaSessionEndTimestamp: Long //Timestamp updated when any event is logged
    private val mediaTimeSpent: Double
        get() { //total seconds between media session start and end time
            return ((this.mediaSessionEndTimestamp - mediaSessionStartTimestamp) / 1000).toDouble()
        }
    private val mediaContentTimeSpent: Double
        get() { //total seconds spent playing content
            return currentPlayheadPosition?.let {
                this.storedPlaybackTime + (System.currentTimeMillis().minus(it) / 1000).toDouble()
            } ?: this.storedPlaybackTime
        }
    private var mediaContentCompleteLimit: Int = 100
    private var mediaContentComplete: Boolean = false //Updates to true triggered by logMediaContentEnd (or if 90% or 95% of the content played), 0 or false if complete milestone not reached or a forced quit.
    private var mediaSessionSegmentTotal: Int = 0 //number incremented with each logSegmentStart
    private var mediaTotalAdTimeSpent: Double = 0.0 //total second sum of ad break time spent
    private val mediaAdTimeSpentRate: Double
        get() { //ad time spent / content time spent x 100
            return if (this.mediaContentTimeSpent != 0.0) {
                this.mediaTotalAdTimeSpent / this.mediaContentTimeSpent * 100
            } else {
                0.0
            }
        }
    private var mediaSessionAdTotal: Int = 0 //number of ads played in the media session - increment on logAdStart
    private var mediaSessionAdObjects: MutableList<String> = ArrayList() //array of unique identifiers for ads played in the media session - append ad_content_ID on logAdStart

    private var currentPlaybackStartTimestamp: Long? = null //Timestamp for beginning of current playback
    private var storedPlaybackTime: Double = 0.0 //On Pause calculate playback time and clear currentPlaybackTime
    private var sessionSummarySent = false // Ensures we only send summary event once

    private var testing = false // Enabled for test cases

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
        if ( 100 >= builder.mediaContentCompleteLimit && builder.mediaContentCompleteLimit > 0) {
            mediaContentCompleteLimit = builder.mediaContentCompleteLimit
        }

        var currentTimestamp = System.currentTimeMillis()
        mediaSessionStartTimestamp = currentTimestamp
        mediaSessionEndTimestamp = currentTimestamp
    }

    /**
     * Start the MediaSession. This method should be called before logging  any other events against this
     * MediaSession instance
     */
    fun logMediaSessionStart(options: Options? = null) {
        sessionId = UUID.randomUUID().toString()
        mediaSessionStartTimestamp = System.currentTimeMillis()
        val mediaSessionEvent = MediaEvent(this, MediaEventName.SESSION_START, options = options)
        logEvent(mediaSessionEvent)
    }

    /**
     * End the MediaSession
     */
    fun logMediaSessionEnd(options: Options? = null) {
        val mediaSessionEvent = MediaEvent(this, MediaEventName.SESSION_END, options = options)
        logEvent(mediaSessionEvent)
        logSessionSummary()
    }

    /**
     * Indicate that the content for the MediaSession has ended. This will NOT end the MediaSession
     */
    fun logMediaContentEnd(options: Options? = null) {
        mediaContentComplete = true
        val mediaSessionEvent = MediaEvent(this, MediaEventName.CONTENT_END, options = options)
        logEvent(mediaSessionEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.PLAY}
     */
    fun logPlay(options: Options? = null) {
        if (currentPlaybackStartTimestamp == null) {
            currentPlaybackStartTimestamp = System.currentTimeMillis()
        }
        val playEvent = MediaEvent(this, MediaEventName.PLAY, options = options)
        logEvent(playEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.PAUSE}
     */
    fun logPause(options: Options? = null) {
        currentPlaybackStartTimestamp?.let {
            storedPlaybackTime += ((System.currentTimeMillis() - it) / 1000)
            currentPlaybackStartTimestamp = null;
        }
        val pauseEvent = MediaEvent(this, MediaEventName.PAUSE, options = options)
        logEvent(pauseEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.SEEK_START}
     *
     * @param position the position, in milliseconds, where the seek event started
     */
    fun logSeekStart(position: Long, options: Options? = null) {
        val seekStartEvent = MediaEvent(this, MediaEventName.SEEK_START, options = options).apply {
            this.seekPosition = position
        }
        logEvent(seekStartEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.SEEK_END}
     *
     * @param position the position, in milliseconds, where the seek event ended
     */
    fun logSeekEnd(position: Long, options: Options? = null) {
        val seekEndEvent = MediaEvent(this, MediaEventName.SEEK_END, options = options).apply {
            this.seekPosition = position
        }
        logEvent(seekEndEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.BUFFER_START}
     *
     * @param position the position, in milliseconds, where the buffer event started
     */
    fun logBufferStart(duration: Long, bufferPercent: Double, position: Long, options: Options? = null) {
        val bufferStart = MediaEvent(this, MediaEventName.BUFFER_START, options = options).apply {
            this.bufferDuration = duration
            this.bufferPosition = position
            this.bufferPercent = bufferPercent
        }
        logEvent(bufferStart)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.BUFFER_END}
     *
     * @param position the position, in milliseconds, where the buffer event ended
     */
    fun logBufferEnd(duration: Long, bufferPercent: Double, position: Long, options: Options? = null) {
        val bufferEnd = MediaEvent(this, MediaEventName.BUFFER_END, options = options).apply {
            this.bufferDuration = duration
            this.bufferPercent = bufferPercent
            this.bufferPosition = position
        }
        logEvent(bufferEnd)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_BREAK_START}
     *
     * @param builder a receiver type closure to build a MediaAdBreak instance
     */
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

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_BREAK_START}
     *
     * @param adBreak the {@link MediaAdBreak} instance
     */
    fun logAdBreakStart(adBreak: MediaAdBreak, options: Options? = null) {
        val adBreakEvent = MediaEvent(this, MediaEventName.AD_BREAK_START, options = options).apply {
            this.adBreak = adBreak
        }
        logEvent(adBreakEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_BREAK_END}
     */
    fun logAdBreakEnd(options: Options? = null) {
        val adBreakEvent = MediaEvent(this, MediaEventName.AD_BREAK_END, options = options)
        logEvent(adBreakEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_START}
     *
     * @param builder a receiver type closure to build a MediaAd instance
     */
    @JvmSynthetic
    fun logAdStart(builder: MediaAd.() -> Unit) {
        logAdStart(null, builder)
    }

    fun logAdStart(options: Options? = null, builder: MediaAd.() -> Unit) {
        val mediaAd = MediaAd()
        mediaAd.builder()
        logAdStart(mediaAd, options)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_START}
     *
     * @param ad the {@link MediaAd} instance
     */
    fun logAdStart(ad: MediaAd, options: Options? = null) {
        ad.adStartTimestamp = System.currentTimeMillis()
        mediaSessionAdTotal += 1
        ad.id?.let { mediaSessionAdObjects.add(it) }
        adContent = ad
        val adStartEvent = MediaEvent(this, MediaEventName.AD_START, options = options).apply {
            mediaAd = ad
        }
        logEvent(adStartEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_END}
     */
    fun logAdEnd(options: Options? = null) {
        adContent?.adStartTimestamp?.let {startTime ->
            val endTime = System.currentTimeMillis()
            adContent?.adEndTimestamp = endTime
            adContent?.adCompleted = true
            mediaTotalAdTimeSpent += ((endTime - startTime) / 1000)
        }
        val adEndEvent = MediaEvent(this, MediaEventName.AD_END, options = options)
        logEvent(adEndEvent)

        logAdSummary(adContent)
    }

    fun logAdClick(options: Options? = null) {
        MediaEvent(this, MediaEventName.AD_CLICK, options = options).let {
            it.mediaAd = adContent
            logEvent(it)
        }
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.AD_SKIP}
     */
    fun logAdSkip(options: Options? = null) {
        adContent?.adStartTimestamp?.let {startTime ->
            val endTime = System.currentTimeMillis()
            adContent?.adEndTimestamp = endTime
            adContent?.adSkipped = true
            mediaTotalAdTimeSpent += ((endTime - startTime) / 1000)
        }
        val adSkipEvent = MediaEvent(this, MediaEventName.AD_SKIP, options = options)
        logEvent(adSkipEvent)

        logAdSummary(adContent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.SEGMENT_START}
     *
     * @param builder a receiver type closure to build a MediaSegment instance
     */
    @JvmSynthetic
    fun logSegmentStart(builder: MediaSegment.() -> Unit) {
        logSegmentStart(null, builder)
    }

    fun logSegmentStart(options: Options? = null, builder: MediaSegment.() -> Unit) {
        mediaSessionSegmentTotal += 1
        val mediaSegment = MediaSegment()
        mediaSegment.builder()
        segment = mediaSegment
        segment?.segmentStartTimestamp = System.currentTimeMillis()
        logSegmentStart(mediaSegment, options)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.SEGMENT_START}
     *
     * @param ad the {@link MediaSegment} instance
     */
    fun logSegmentStart(segment: MediaSegment, options: Options? = null) {
        val segmentStartEvent = MediaEvent(this, MediaEventName.SEGMENT_START, options = options).apply {
            this.segment = segment
        }
        logEvent(segmentStartEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.SEGMENT_SKIP}
     */
    fun logSegmentSkip(options: Options? = null) {
        segment?.segmentEndTimestamp = System.currentTimeMillis()
        segment?.segmentSkipped = true
        val segmentSkipEvent = MediaEvent(this, MediaEventName.SEGMENT_SKIP, options = options)
        logEvent(segmentSkipEvent)

        logSegmentSummary(segment)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.SEGMENT_END}
     */
    fun logSegmentEnd(options: Options? = null) {
        segment?.segmentEndTimestamp = System.currentTimeMillis()
        segment?.segmentCompleted = true
        val segmentEndEvent = MediaEvent(this, MediaEventName.SEGMENT_END, options = options)
        logEvent(segmentEndEvent)

        logSegmentSummary(segment)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.UPDATE_PLAYHEAD_POSITION}. This will also
     * update the internal currentPlayheadPosition state.
     */
    fun logPlayheadPosition(playheadPosition: Long) {
        currentPlayheadPosition = playheadPosition
        val playheadEvent = MediaEvent(this, MediaEventName.UPDATE_PLAYHEAD_POSITION).apply {
            this.playheadPosition = playheadPosition
        }
        logEvent(playheadEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.UPDATE_QOS}. The QOS object will be treated as
     * stateless, and some kits may replace `null` values with the default primitive
     *
     * @param builder a receiver type closure to build a MediaQoS instance
     */
    @JvmSynthetic
    fun logQos(builder: MediaQoS.() -> Unit) {
        logQos(null, builder)
    }

    fun logQos(options: Options? = null, builder: MediaQoS.() -> Unit) {
        val mediaQos = MediaQoS()
        mediaQos.builder()
        logQos(mediaQos, options)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.ERROR}. For these events, you will be able
     * to query the {@link MediaEvent#error} field
     *
     * @param message the error message
     * @param attributes a Map of addition information about the error
     */
    fun logError(message: String, attributes: Map<String, Any?> = mapOf(), options: Options? = null) {
        val errorEvent = MediaEvent(this, MediaEventName.ERROR, options = options).apply {
            error = MediaError(message, attributes)
        }
        logEvent(errorEvent)
    }

    /**
     * Log a MediaEvent of type {@link MediaEventName.UPDATE_QOS}. The MediaSession maintains QoS state internally,
     * and will merge the provided MediaQoS argument with the internal object, replacing any null fields in the argument object with
     * the value of the corresponding field in the internal object, provided the internal object's field is non null.
     *
     * Some Kits may replace `null` values with the default primitive
     *
     * @param qos the {@link MediaQoS} instance
     */
    fun logQos(qos: MediaQoS, options: Options? = null) {
        this.sessionQoS = MediaQoS(
            startupTime = qos.startupTime ?: this.sessionQoS.startupTime,
            bitRate = qos.bitRate ?: this.sessionQoS.bitRate,
            fps = qos.fps ?: this.sessionQoS.fps,
            droppedFrames = qos.droppedFrames ?: this.sessionQoS.droppedFrames
        )
        val qosEvent = MediaEvent(this, MediaEventName.UPDATE_QOS, options = options).apply {
            this.qos = sessionQoS
        }
        logEvent(qosEvent)
    }

    /**
     * Create a MPEvent in the current MediaSession context. The {@link BaseEvent#customAttributes()}
     * will be populated with the relevant {@link MediaContent} fields which belong to the MediaSession
     * instance. This includes "title", "mediaContentId", "duration", "streamType" and "contentType"
     */
    fun buildMPEvent(eventName: String, customAttributes: Map<String, String>?): MPEvent {
        val eventAttributes = attributes
        customAttributes?.let { eventAttributes.putAll(it) }
        return MPEvent.Builder(eventName, MParticle.EventType.Media)
            .customAttributes(customAttributes)
            .build()
    }

    /**
     * Register a MediaEventListener instance which will receive a callback everytime a {@link MediaEvent}
     * is generated. The callback will occur before the {@link MediaEvent} is logged in the Core SDK
     */
    fun setMediaEventListener(listener: MediaEventListener) {
        mediaEventListener = { mediaEvent -> listener.onLogMediaEvent(mediaEvent)}
    }

    private fun logSessionSummary() {
        if (!sessionSummarySent) {
            var customAttributes: MutableMap<String, String> = mutableMapOf()
            sessionId?.let {
                customAttributes[mediaSessionIdKey] = it
            }
            customAttributes[startTimestampKey] = mediaSessionStartTimestamp.toString()
            customAttributes[endTimestampKey] = mediaSessionEndTimestamp.toString()
            customAttributes[contentIdKey] = mediaContentId
            customAttributes[contentTitleKey] = title
            customAttributes[mediaTimeSpentKey] = mediaTimeSpent.toString()
            customAttributes[contentTimeSpentKey] = mediaContentTimeSpent.toString()
            customAttributes[contentCompleteKey] = mediaContentComplete.toString()
            customAttributes[totalSegmentsKey] = mediaSessionSegmentTotal.toString()
            customAttributes[totalAdTimeSpentKey] = mediaTotalAdTimeSpent.toString()
            customAttributes[adTimeSpentRateKey] = mediaAdTimeSpentRate.toString()
            customAttributes[totalAdsKey] = mediaSessionAdTotal.toString()
            customAttributes[adIDsKey] = mediaSessionAdObjects.toString()

            var summaryEvent = buildMPEvent(MediaSessionSummary, customAttributes)
            mparticleInstance?.logEvent(summaryEvent)

            sessionSummarySent = true
        }
    }

    private fun logSegmentSummary(summary: MediaSegment?) {
        segment?.segmentStartTimestamp?.let { segmentStartTimestamp ->
            summary?.let { segmentSummary ->
                var segmentEndTimestamp = segment?.segmentEndTimestamp
                if (segmentEndTimestamp == null) {
                    segmentEndTimestamp = System.currentTimeMillis()
                    segmentSummary.segmentEndTimestamp = segmentEndTimestamp
                }

                var customAttributes: MutableMap<String, String> = mutableMapOf()
                sessionId?.let {
                    customAttributes[mediaSessionIdKey] = it
                }
                customAttributes[mediaContentId] = mediaContentId
                customAttributes[segmentIndexKey] = segmentSummary.index.toString()
                segmentSummary.title?.let {
                    customAttributes[segmentTitleKey] = it
                }
                customAttributes[segmentStartTimestampKey] =
                    segmentSummary.segmentStartTimestamp.toString()
                customAttributes[segmentEndTimestampKey] =
                    segmentSummary.segmentEndTimestamp.toString()
                customAttributes[segmentTimeSpentKey] =
                    (((segmentEndTimestamp - segmentStartTimestamp) / 1000).toDouble()).toString()
                customAttributes[segmentSkippedKey] = segmentSummary.segmentSkipped.toString()
                customAttributes[segmentCompletedKey] = segmentSummary.segmentCompleted.toString()

                var summaryEvent = buildMPEvent(MediaSegmentSummary, customAttributes)
                mparticleInstance?.logEvent(summaryEvent)
            }
            segment = null
        }
    }

    private fun logAdSummary(content: MediaAd?) {
        content?.let { ad ->
            ad.adStartTimestamp?.let { startTime ->
                val endTime = System.currentTimeMillis()
                ad.adEndTimestamp = endTime
                mediaTotalAdTimeSpent += ((endTime - startTime) / 1000).toDouble()
            }

            val customAttributes: MutableMap<String, String> = mutableMapOf()
            sessionId?.let {
                customAttributes[mediaSessionIdKey] = it
            }
            ad.id?.let {
                customAttributes[adContentIdKey] = it
            }
            ad.adStartTimestamp?.let {
                customAttributes[adContentStartTimestampKey] = it.toString()
            }
            ad.adEndTimestamp?.let {
                customAttributes[adContentEndTimestampKey] = it.toString()
            }
            ad.title?.let {
                customAttributes[adContentTitleKey] = it
            }
            customAttributes[adContentSkippedKey] = ad.adSkipped.toString()
            customAttributes[adContentCompletedKey] = ad.adCompleted.toString()

            var summaryEvent = buildMPEvent(MediaAdSummary, customAttributes)
            mparticleInstance?.logEvent(summaryEvent)

            adContent = null
        }
    }

    protected fun logEvent(mediaEvent: MediaEvent) {
        if (mparticleInstance == null) {
            mparticleInstance = MParticle.getInstance()
        }

        mediaSessionEndTimestamp = System.currentTimeMillis()
        if (mediaContentCompleteLimit < 100 && (duration != null && currentPlayheadPosition != null) && ((currentPlayheadPosition!! / duration!!) >= (mediaContentCompleteLimit / 100))) {
            mediaContentComplete = true
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
        var mediaContentCompleteLimit = 100
            @JvmSynthetic
            set
        /**
         * Set the Title of the {@link MediaContent} for this {@link MediaSession}
         */
        fun title(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * Set the MediaContentId of the {@link MediaContent} for this {@link MediaSession}
         */
        fun mediaContentId(mediaContentId: String): Builder {
            this.mediaContentId = mediaContentId
            return this
        }

        /**
         * Set the Duration of the {@link MediaContent} for this {@link MediaSession}
         */
        fun duration(duration: Long): Builder {
            this.duration = duration
            return this
        }

        /**
         * Set the StreamType of the {@link MediaContent} for this {@link MediaSession}
         */
        fun streamType(streamType: String): Builder {
            this.streamType = streamType
            return this
        }

        /**
         * Set the ContentType of the {@link MediaContent} for this {@link MediaSession}
         */
        fun contentType(contentType: String): Builder {
            this.contentType = contentType
            return this
        }

        /**
         * Indicate whether this {@link MediaSession} should automatically log generated {@link MediaEvent}
         * instances to Kits
         */
        fun logMediaEvents(shouldLog: Boolean): Builder {
            this.logMediaEvents = shouldLog
            return this;
        }

        /**
         * Indicate whether this {@link MediaSession} should automatically log generated {@link MPEvent}
         * instances and to the MParticle Server
         */
        fun logMPEvents(shouldLog: Boolean): Builder {
            this.logMPEvents = shouldLog
            return this
        }

        /**
         * Set the Percentage of the {@link MediaContent} the user needs to progress to for content to be marked complete
         */
        fun mediaContentCompleteLimit(contentCompleteLimit: Int): Builder {
            this.mediaContentCompleteLimit = contentCompleteLimit
            return this
        }

        /**
         * Build to a {@link MediaSession}
         */
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