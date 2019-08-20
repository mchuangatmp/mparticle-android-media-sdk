package com.mparticle;

import com.mparticle.events.ContentType;
import com.mparticle.events.StreamType;

public class APISampleJava {

    public void main() {
        MediaSession mediaSession = MediaSession.builder()
                .duration(123L)
                .title("Media Title ")
                .mediaContentId("abc123")
                .contentType(ContentType.AUDIO)
                .streamType(StreamType.LIVE_STEAM)
                .build();
    }
}