package testpckg

import com.mparticle.MParticle
import com.mparticle.media.MediaSession
import com.mparticle.media.events.*
import com.mparticle.media.events.EventAttributes.CONTENT_EPISODE
import com.mparticle.media.events.EventAttributes.PLAYER_NAME
import org.junit.Before
import org.junit.Test


class APISampleKotlin {
    lateinit var mediaSession: MediaSession

    @Before
    fun before() {
        //log MPEvents, don't log MediaEvents
        mediaSession = MediaSession.builder {
            title = "Media Title"
            mediaContentId = "123"
            duration = 1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO

            logMediaEvents = false
            logMPEvents = true
        }
    }

    @Test
    fun buildMPEvent() {
        //log a custom event, type "Milestone" with customAttributes ["type", "95%"]
        mediaSession.buildMPEvent(
            "Milestone", mapOf(
                "type" to "95%"
            )
        )
    }

    @Test
    fun logAdStartBuilder() {
//log some MediaEvents
        mediaSession.logAdStart {
            id = "4423210"
            advertiser = "Moms Friendly Robot Company"
            title = "What?! Nobody rips off my kids but me!"
            campaign = "MomCorp Galactic Domination Plot 3201"
            duration = 60000
            creative = "A Fistful of Dollars"
            siteId = "moms"
            placement = "first"
            position = 0
        }
    }

    @Test
    fun mediaEventListener() {
        mediaSession.mediaEventListener = { mediaEvent ->
            if (mediaEvent.eventName == MediaEventName.PLAY) {
                val mpEvent = mediaEvent.toMPEvent()
                MParticle.getInstance()?.logEvent(mpEvent)
            }
        }
    }

    @Test
    fun logPlayVariations() {
        mediaSession.logPlay()
        mediaSession.logPlay(Options(currentPlayheadPosition = 120000))
    }

    @Test
    fun logAdBreakStartBuilder() {
        mediaSession.logAdBreakStart {
            id = "123456"
            title = "pre-roll"
            duration = 6000
        }
    }

    @Test
    fun optionsVariations() {
        Options(
            100,
            mapOf(CONTENT_EPISODE to "episode1")
        )

        Options {
            customAttributes = mapOf(
                "isFullScreen" to "true",
                CONTENT_EPISODE to "episode1",
                PLAYER_NAME to "JWPlayer"
            )
            currentPlayheadPosition = 100
        }
    }

    @Test
    fun logError() {
        mediaSession.logError("some error")
        mediaSession.logError("some error", mapOf("with" to "attributes"))
    }
}