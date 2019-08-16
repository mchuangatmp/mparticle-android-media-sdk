package testpckg

import com.mparticle.MediaSession
import com.mparticle.events.MediaType
import com.mparticle.events.StreamType


fun main() {
    val mediaSession = MediaSession.builder {
        title = "hello"
        mediaContentId = "123"
        duration = 1000
        streamType = StreamType.LIVE_STEAM
        mediaType = MediaType.VIDEO
    }

}