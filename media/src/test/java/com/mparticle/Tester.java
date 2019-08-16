package com.mparticle;

import com.mparticle.events.MediaType;
import com.mparticle.events.StreamType;

public class Tester {


    public void main() {
        MediaSession mediaSession = MediaSession.builder()
                .duration(123)
                .title("asdv ")
                .mediaType(MediaType.AUDIO)
                .streamType(StreamType.LIVE_STEAM)
                .build();
    }
}