package testpckg

import com.mparticle.MParticle
import com.mparticle.media.MediaSession
import com.mparticle.media.events.*
import com.mparticle.media.events.EventAttributes.CONTENT_EPISODE
import com.mparticle.media.events.EventAttributes.PLAYER_NAME


fun main() {


    //log MPEvents, don't log MediaEvents
    val mediaSession = MediaSession.builder {
        title = "Media Title"
        mediaContentId = "123"
        duration = 1000
        streamType = StreamType.LIVE_STEAM
        contentType = ContentType.VIDEO

        logMediaEvents = false
        logMPEvents = true
    }

    //log a custom event, type "Milestone" with customAttributes ["type", "95%"]
    val mpEvent = mediaSession.buildMPEvent("Milestone", mapOf(
        "type" to "95%"
    ))
    MParticle.getInstance()?.logEvent(mpEvent)


    //listen for Media events type PLAY, PAUSE, CONTENT_END
    mediaSession.mediaEventListener = { mediaEvent ->
        if (mediaEvent.eventName == MediaEventName.PLAY) {
            val mpEvent = mediaEvent.toMPEvent()
            MParticle.getInstance()?.logEvent(mpEvent)
        }
    }

    var options = Options(currentPlayheadPosition = 120000)

    mediaSession.logPlay(options)




    //log some MediaEvents
    mediaSession.logAdStart {
        id = "4423210"
        advertiser = "Moms Friendly Robot Company"
        title = "What?! Nobody rips off my kids but me!"
        campaign = "MomCorp Galactic Domination Plot 3201"
        duration = 60000
        creative = "A Fistful of Dollars"
        siteId = "moms"
        placement = 0
    }
     options = Options(
        customAttributes = mapOf(
            "isFullScreen" to "true",
            CONTENT_EPISODE to "episode1",
            PLAYER_NAME to "JWPlayer"
        )
    )

    mediaSession.logAdBreakStart {
        id = "123456"
        title = "pre-roll"
        duration = 6000
    }
}