package com.mparticle.media.events

import com.mparticle.BaseEvent
import com.mparticle.MPEvent
import com.mparticle.MParticle
import com.mparticle.media.MediaSession
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


open class MediaEvent(
    session: MediaSession,
    val eventName: String = "Unknown",
    val timeStamp: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString(),
    val options: Options? = null
): BaseEvent(Type.MEDIA) {

    var sessionId: String? = null
    var mediaContent: MediaContent
    var playheadPosition: Long?

    var qos: MediaQoS? = null
    var mediaAd: MediaAd? = null
    var segment: MediaSegment? = null
    var adBreak: MediaAdBreak? = null
    var seekPosition: Long? = null
    var bufferDuration: Long? = null
    var bufferPercent: Double? = null
    var bufferPosition: Long? = null

    init {
        sessionId = session.sessionId
        mediaContent = MediaContent().apply {
            name = session.title
            contentId = session.mediaContentId
            duration = session.duration
            contentType = session.contentType
            streamType = session.streamType
        }
        playheadPosition = session.currentPlayheadPosition
        if (options != null) {
            if (options.currentPlayheadPosition != null) {
                playheadPosition = options.currentPlayheadPosition
                session.currentPlayheadPosition = options.currentPlayheadPosition
            }
            if (!options.customAttributes.isEmpty()) {
                customAttributes = HashMap(options.customAttributes)
            }
        }
    }
    
    fun toMPEvent(): MPEvent {
        val mediaAttributes = getSessionAttributes()
        mediaAttributes.putAll(getEventAttributes())
        mediaAttributes.putAll(customAttributes?: mapOf())
        return MPEvent.Builder(eventName, MParticle.EventType.Media)
            .customAttributes(mediaAttributes)
            .build()
    }

    internal fun getSessionAttributes(): MutableMap<String, String> {
        val sessionAttributes = HashMap<String, String>()
        sessionAttributes.putIfNotNull(MediaAttributeKeys.MEDIA_SESSION_ID, sessionId)

        sessionAttributes.putIfNotNull(MediaAttributeKeys.PLAYHEAD_POSITION, playheadPosition)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.TITLE, mediaContent.name)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.CONTENT_ID, mediaContent.contentId)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.DURATION, mediaContent.duration)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.STREAM_TYPE, mediaContent.streamType)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.CONTENT_TYPE, mediaContent.contentType)
        return sessionAttributes;
    }

    internal fun getEventAttributes(): MutableMap<String, String> {
        val eventAttributes = HashMap<String, String>()

        eventAttributes.putIfNotNull(MediaAttributeKeys.SEEK_POSITION, seekPosition)
        eventAttributes.putIfNotNull(MediaAttributeKeys.BUFFER_DURATION, bufferDuration)
        eventAttributes.putIfNotNull(MediaAttributeKeys.BUFFER_PERCENT, bufferPercent)
        eventAttributes.putIfNotNull(MediaAttributeKeys.BUFFER_POSITION, bufferPosition)

        qos?.also { qos ->
            eventAttributes.putIfNotNull(MediaAttributeKeys.QOS_BITRATE, qos.bitRate)
            eventAttributes.putIfNotNull(MediaAttributeKeys.QOS_DROPPED_FRAMES, qos.droppedFrames)
            eventAttributes.putIfNotNull(MediaAttributeKeys.QOS_FRAMES_PER_SECOND, qos.fps)
            eventAttributes.putIfNotNull(MediaAttributeKeys.QOS_STARTUP_TIME, qos.startupTime)
        }
        mediaAd?.also { mediaAd ->
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_TITLE, mediaAd.title)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_ID, mediaAd.id)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_ADVERTISING_ID, mediaAd.advertiser)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_CAMPAIGN, mediaAd.campaign)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_CREATIVE, mediaAd.creative)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_SITE_ID, mediaAd.siteId)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_DURATION, mediaAd.duration)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_PLACEMENT, mediaAd.placement)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_POSITION, mediaAd.position)
        }
        segment?.also { segment ->
            eventAttributes.putIfNotNull(MediaAttributeKeys.SEGMENT_TITLE, segment.title)
            eventAttributes.putIfNotNull(MediaAttributeKeys.SEGMENT_INDEX, segment.index)
            eventAttributes.putIfNotNull(MediaAttributeKeys.SEGMENT_DURATION, segment.duration)
        }
        adBreak?.also { adBreak ->
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_BREAK_TITLE, adBreak.title)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_BREAK_DURATION, adBreak.duration)
            eventAttributes.putIfNotNull(MediaAttributeKeys.AD_BREAK_ID, adBreak.id)
        }
        return eventAttributes
    }

    override fun toString(): String {
        val json = JSONObject()
        json.put("type", eventName)
        json.put("id", id)
        if (playheadPosition != null) {
            json.put("playhead position", playheadPosition!!)
        }
        if (seekPosition != null) {
            json.put("seek position", seekPosition!!)
        }
        if (bufferDuration != null) {
            json.put("buffer duration", bufferDuration!!)
        }
        if (bufferPercent != null) {
            json.put("buffer percent", bufferPercent!!)
        }
        if (bufferPosition != null) {
            json.put("buffer position", bufferPosition!!)
        }
        if (qos != null) {
            json.put("qos", JSONObject()
                .put("bit rate", qos?.bitRate)
                .put("dropped frames", qos?.droppedFrames)
                .put("fps", qos?.fps)
                .put("startup time", qos?.startupTime))
        }
        if (mediaAd != null) {
            json.put("media ad", JSONObject()
                .put("title", mediaAd?.title)
                .put("id", mediaAd?.id)
                .put("advertiser", mediaAd?.advertiser)
                .put("campaign", mediaAd?.campaign)
                .put("creative", mediaAd?.creative)
                .put("siteId", mediaAd?.siteId)
                .put("duration", mediaAd?.duration)
                .put("placement", mediaAd?.placement))
        }
        if (segment != null) {
            json.put("segment", JSONObject()
                .put("title", segment?.title)
                .put("index", segment?.index)
                .put("duration", segment?.duration))
        }
        if (adBreak != null) {
            json.put("adBreak", JSONObject()
                .put("title", adBreak?.title)
                .put("duration", adBreak?.duration))
        }
        json.put("Media Content", JSONObject()
            .put("name", mediaContent.name)
            .put("id", mediaContent.contentId)
            .put("duration", mediaContent.duration)
            .put("stream type", mediaContent.streamType)
            .put("content type", mediaContent.contentType))

        json.put("session id", sessionId)
        json.put("timestamp", timeStamp)
        return json.toString()
    }
    
    fun HashMap<String, String>.putIfNotNull(key: String, value: Any?) {
        if (value != null) {
            put(key, value.toString())
        }
    }
}


