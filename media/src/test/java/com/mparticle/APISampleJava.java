package com.mparticle;

import com.mparticle.events.ContentType;
import com.mparticle.events.MediaEvent;
import com.mparticle.events.StreamType;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class APISampleJava {

    public void main() {
        MediaSession mediaSession = MediaSession.builder()
                .duration(123L)
                .title("Media Title ")
                .mediaContentId("abc123")
                .contentType(ContentType.AUDIO)
                .streamType(StreamType.LIVE_STEAM)
                .build();
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