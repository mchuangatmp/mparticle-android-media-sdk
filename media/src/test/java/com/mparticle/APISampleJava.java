package com.mparticle;

import com.mparticle.media.MediaSession;
import com.mparticle.media.events.ContentType;
import com.mparticle.media.events.MediaEvent;
import com.mparticle.media.events.StreamType;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class APISampleJava {
    private MediaSession mediaSession = null;

    @Before
    public void before() {
        mediaSession = MediaSession.builder()
                .duration(123L)
                .title("Media Title ")
                .mediaContentId("abc123")
                .contentType(ContentType.AUDIO)
                .streamType(StreamType.LIVE_STEAM)
                .build();
    }

    @Test
    public void main() {
        mediaSession.setMediaEventListener(new Function1<MediaEvent, Unit>() {
            @Override
            public Unit invoke(MediaEvent mediaEvent) {
                MPEvent mpEvent = mediaEvent.toMPEvent();
                MParticle.getInstance().logEvent(mpEvent);
                return Unit.INSTANCE;
            }
        });
    }
}