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
    var error: MediaError? = null

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
                customAttributes = options.customAttributes.toMap()
            }
        }
    }
    
    fun toMPEvent(): MPEvent {
        val mediaAttributes = getSessionAttributes()
        mediaAttributes.putAll(getEventAttributes())
        mediaAttributes.putAll(customAttributeStrings?: mapOf())
        return MPEvent.Builder(eventName, MParticle.EventType.Media)
            .customAttributes(mediaAttributes)
            .build()
    }

    internal fun getSessionAttributes(): MutableMap<String, Any?> {
        val sessionAttributes = HashMap<String, Any?>()
        sessionAttributes.putIfNotNull(MediaAttributeKeys.MEDIA_SESSION_ID, sessionId)

        sessionAttributes.putIfNotNull(MediaAttributeKeys.PLAYHEAD_POSITION, playheadPosition)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.TITLE, mediaContent.name)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.CONTENT_ID, mediaContent.contentId)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.DURATION, mediaContent.duration)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.STREAM_TYPE, mediaContent.streamType)
        sessionAttributes.putIfNotNull(MediaAttributeKeys.CONTENT_TYPE, mediaContent.contentType)
        return sessionAttributes;
    }

    internal fun getEventAttributes(): MutableMap<String, Any?> {
        val eventAttributes = HashMap<String, Any?>()

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
        error?.also { error ->
            eventAttributes.putIfNotNull(MediaAttributeKeys.ERROR_MESSAGE, error.message)
            error.attributes.also { attributes ->
                if (attributes.isNotEmpty()) {
                    eventAttributes.putIfNotNull(MediaAttributeKeys.ERROR_ATTRIBUTES, attributes)
                }
            }
        }
        return eventAttributes
    }

    override fun toString(): String {
        val json = JSONObject()
        json.put("type", eventName)
        json.put("id", id)
        playheadPosition?.let {
            json.put("playhead position", it)
        }
        seekPosition?.let {
            json.put("seek position", it)
        }
        bufferDuration?.let {
            json.put("buffer duration", it)
        }
        bufferPercent?.let {
            json.put("buffer percent", it)
        }
        bufferPosition?.let {
            json.put("buffer position", it)
        }
        qos?.apply {
            json.put("qos", JSONObject()
                .put("bit rate", bitRate)
                .put("dropped frames", droppedFrames)
                .put("fps", fps)
                .put("startup time", startupTime))
        }
        mediaAd?.apply { 
            json.put("media ad", JSONObject()
                .put("title", title)
                .put("id", id)
                .put("advertiser", advertiser)
                .put("campaign", campaign)
                .put("creative", creative)
                .put("siteId", siteId)
                .put("duration", duration)
                .put("placement", placement))
                .put("position", position)
        }
        segment?.apply {
            json.put("segment", JSONObject()
                .put("title", title)
                .put("index", index)
                .put("duration", duration))
        }
        adBreak?.apply {
            json.put("adBreak", JSONObject()
                .put("title", title)
                .put("duration", duration))
        }
        json.put("Media Content", JSONObject()
            .put("name", mediaContent.name)
            .put("id", mediaContent.contentId)
            .put("duration", mediaContent.duration)
            .put("stream type", mediaContent.streamType)
            .put("content type", mediaContent.contentType))

        json.put("session id", sessionId)
        json.put("timestamp", timeStamp)
        error?.apply {
            json.put("error message", message)
            if (attributes.isNotEmpty()) {
                json.put("error attributes", attributes)
            }
        }
        return json.toString()
    }
    
    fun <T> HashMap<String, T>.putIfNotNull(key: String, value: T?) {
        value?.also {
            put(key, it)
        }
    }
}


