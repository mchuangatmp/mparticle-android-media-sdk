package com.mparticle.events

import com.mparticle.BaseEvent
import com.mparticle.MediaSession
import org.json.JSONObject
import java.util.*


open class MediaEvent(val eventType: MediaEventType,
                      session: MediaSession,
                      val timeStamp: Long = System.currentTimeMillis(),
                      val id: String = UUID.randomUUID().toString()): BaseEvent(eventType) {

    var sessionId: String
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
    }

    override fun toString(): String {
        val json = JSONObject()
        json.put("type", eventType.name)
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
                .put("tag", adBreak?.title)
                .put("duration", adBreak?.duration)
                .put("current playback time", adBreak?.currentPlaybackTime))
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
}


