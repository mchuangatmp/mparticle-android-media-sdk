package com.mparticle

import android.app.Application
import com.mparticle.identity.BaseIdentityTask
import com.mparticle.media.MediaSession
import com.mparticle.media.events.*
import com.mparticle.testutils.RandomUtils
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test
import java.util.*


class MediaEventToCustomEvent {
    val random = Random()
    val randomUtils = RandomUtils()

    val mediaSession = MediaSession.builder {
        contentType = ContentType.VIDEO
        streamType = StreamType.ON_DEMAND
        duration = 1234
        title = "title"
        mediaContentId = "abc123"
    }

    @Test
    fun testMediaSession() {
        assertEquals(ContentType.VIDEO, mediaSession.contentType)
        assertEquals(StreamType.ON_DEMAND, mediaSession.streamType)
        assertEquals("title", mediaSession.title)
        assertEquals("abc123", mediaSession.mediaContentId)
        assertEquals(1234L, mediaSession.duration)
    }
    
    @Test
    fun testEmptyEvent() {
        val mediaEvent = MediaEvent(mediaSession, MediaEventName.UPDATE_PLAYHEAD_POSITION)
        val customEvent = mediaEvent.toMPEvent()
        assertNotNull(customEvent)
        assertEquals(5, customEvent.customAttributes?.size)
        customEvent.customAttributes?.apply {
            assertMediaSessionPresent(mediaSession, customEvent.customAttributes!!)
        }
    }

    @Test
    fun testEmptyEventEmptyObjects() {
        val mediaEvent = MediaEvent(mediaSession, MediaEventName.UPDATE_PLAYHEAD_POSITION).apply {
            qos = MediaQoS()
            mediaAd = MediaAd()
            adBreak = MediaAdBreak()
            segment = MediaSegment()
        }
        val customEvent = mediaEvent.toMPEvent()
        assertNotNull(customEvent)
        assertEquals(5, customEvent.customAttributes?.size)
        customEvent.customAttributes?.apply {
            assertMediaSessionPresent(mediaSession, customEvent.customAttributes!!)
        }
    }

    @Test
    fun testEmptyEventSessionStarted() {
        mediaSession.logMediaSessionStart()

        val mediaEvent = MediaEvent(mediaSession, MediaEventName.UPDATE_PLAYHEAD_POSITION)
        val customEvent = mediaEvent.toMPEvent()
        assertNotNull(customEvent)
        assertEquals(6, customEvent.customAttributes?.size)
        customEvent.customAttributes?.apply {
            assertMediaSessionPresent(mediaSession, customEvent.customAttributes!!)
            assertEquals(mediaSession.sessionId, get(MediaAttributeKeys.MEDIA_SESSION_ID))
        }
    }

    @Test
    fun testSetPlayhead() {
        val playhead = random.nextLong()
        mediaSession.logMediaSessionStart()
        mediaSession.logPlayheadPosition(playhead)

        val mediaEvent = MediaEvent(mediaSession, MediaEventName.UPDATE_PLAYHEAD_POSITION)
        val customEvent = mediaEvent.toMPEvent()
        assertNotNull(customEvent)
        assertEquals(7, customEvent.customAttributes?.size)
        customEvent.customAttributes?.apply {
            assertMediaSessionPresent(mediaSession, customEvent.customAttributes!!)
            assertEquals(mediaSession.sessionId, get(MediaAttributeKeys.MEDIA_SESSION_ID))
            assertEquals(playhead.toString(), get(MediaAttributeKeys.PLAYHEAD_POSITION))
        }
    }

    @Test
    fun fullEvent() {
        val mediaEvent = MediaEvent(mediaSession, MediaEventName.UPDATE_PLAYHEAD_POSITION).apply {
            qos = MediaQoS().apply {
                startupTime = random.nextLong()
                droppedFrames = random.nextInt()
                bitRate = random.nextInt()
                fps = random.nextInt()
            }
            mediaAd = MediaAd().apply {
                creative = randomUtils.getAlphaNumericString(24)
                title = randomUtils.getAlphaNumericString(24)
                id = randomUtils.getAlphaNumericString(24)
                advertiser = randomUtils.getAlphaNumericString(24)
                duration = random.nextLong()
                placement = randomUtils.getAlphaNumericString(24)
                position = random.nextInt()
                siteId = randomUtils.getAlphaNumericString(24)
            }
            adBreak = MediaAdBreak().apply {
                duration = random.nextLong()
                id = randomUtils.getAlphaNumericString(24)
                title = randomUtils.getAlphaNumericString(24)
            }
            segment = MediaSegment().apply {
                duration = random.nextLong()
                index = random.nextInt()
                title = randomUtils.getAlphaNumericString(24)
            }
        }
        val customEvent = mediaEvent.toMPEvent()
        assertNotNull(customEvent)
        customEvent.customAttributes!!.apply {
            assertMediaSessionPresent(mediaSession, customEvent.customAttributes!!)

            assertEquals(mediaEvent.qos!!.startupTime.toString(), remove(MediaAttributeKeys.QOS_STARTUP_TIME))
            assertEquals(mediaEvent.qos!!.droppedFrames.toString(), remove(MediaAttributeKeys.QOS_DROPPED_FRAMES))
            assertEquals(mediaEvent.qos!!.fps.toString(), remove(MediaAttributeKeys.QOS_FRAMES_PER_SECOND))
            assertEquals(mediaEvent.qos!!.bitRate.toString(), remove(MediaAttributeKeys.QOS_BITRATE))

            assertEquals(mediaEvent.mediaAd!!.creative, remove(MediaAttributeKeys.AD_CREATIVE))
            assertEquals(mediaEvent.mediaAd!!.title, remove(MediaAttributeKeys.AD_TITLE))
            assertEquals(mediaEvent.mediaAd!!.id, remove(MediaAttributeKeys.AD_ID))
            assertEquals(mediaEvent.mediaAd!!.siteId, remove(MediaAttributeKeys.AD_SITE_ID))
            assertEquals(mediaEvent.mediaAd!!.advertiser, remove(MediaAttributeKeys.AD_ADVERTISING_ID))
            assertEquals(mediaEvent.mediaAd!!.campaign, remove(MediaAttributeKeys.AD_CAMPAIGN))
            assertEquals(mediaEvent.mediaAd!!.duration.toString(), remove(MediaAttributeKeys.AD_DURATION))
            assertEquals(mediaEvent.mediaAd!!.placement, remove(MediaAttributeKeys.AD_PLACEMENT))
            assertEquals(mediaEvent.mediaAd!!.position.toString(), remove(MediaAttributeKeys.AD_POSITION))

            assertEquals(mediaEvent.adBreak!!.duration.toString(), remove(MediaAttributeKeys.AD_BREAK_DURATION))
            assertEquals(mediaEvent.adBreak!!.title, remove(MediaAttributeKeys.AD_BREAK_TITLE))
            assertEquals(mediaEvent.adBreak!!.id, remove(MediaAttributeKeys.AD_BREAK_ID))
            
            assertEquals(mediaEvent.segment!!.duration.toString(), remove(MediaAttributeKeys.SEGMENT_DURATION))
            assertEquals(mediaEvent.segment!!.index.toString(), remove(MediaAttributeKeys.SEGMENT_INDEX))
            assertEquals(mediaEvent.segment!!.title, remove(MediaAttributeKeys.SEGMENT_TITLE))

            assertEquals(0, size)
        }
    }

    fun assertMediaSessionPresent(mediaSession: MediaSession, customAttributes: MutableMap<String, String>) {
        assertEquals(mediaSession.contentType, customAttributes.remove(MediaAttributeKeys.CONTENT_TYPE))
        assertEquals(mediaSession.streamType, customAttributes.remove(MediaAttributeKeys.STREAM_TYPE))
        assertEquals(mediaSession.title, customAttributes.remove(MediaAttributeKeys.TITLE))
        assertEquals(mediaSession.mediaContentId, customAttributes.remove(MediaAttributeKeys.CONTENT_ID))
        assertEquals(mediaSession.duration.toString(), customAttributes.remove(MediaAttributeKeys.DURATION))
    }
}