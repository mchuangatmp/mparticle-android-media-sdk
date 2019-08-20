package testpckg

import com.mparticle.MediaSession
import com.mparticle.events.ContentType
import com.mparticle.events.MediaAd
import com.mparticle.events.StreamType


fun main() {
    val mediaSession = MediaSession.builder {
        title = "Media Title"
        mediaContentId = "123"
        duration = 1000
        streamType = StreamType.LIVE_STEAM
        contentType = ContentType.VIDEO
    }
    mediaSession.logAdStart {
        id = "4423210"
        advertiser= "Moms Friendly Robot Company"
        title= "What?! Nobody rips off my kids but me!"
        campaign= "MomCorp Galactic Domination Plot 3201"
        this.duration = 60000
        creative= "A Fishful of Dollars"
        siteId= "moms"
        placement= 0
    }
    mediaSession.logAdBreakStart {
        id = "123456"
        title = "pre-roll"
        duration = 6000
    }

}