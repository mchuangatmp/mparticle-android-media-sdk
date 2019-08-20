package com.mparticle;

import com.mparticle.events.*;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import kotlin.jvm.JvmSynthetic;
import kotlin.jvm.functions.Function1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MediaSessionLogEventTest {

    Random random = new Random();

    @Test
    public void testLogEventToCore() throws InvocationTargetException, IllegalAccessException {
        MockMParticle mparticle = new MockMParticle();

        MediaSession mediaSession = MediaSession.builder(mparticle)
            .title("hello")
            .mediaContentId("123")
            .duration(1000)
            .streamType(StreamType.LIVE_STEAM)
            .contentType(ContentType.VIDEO)
                .build();


        List<Method> methods = new ArrayList<>();
        for(Method method: MediaSession.class.getMethods()) {
            if (method.getName().startsWith("log")
                    && !method.getName().endsWith("$default")) {
                methods.add(method);
            }
        }

        for (Method method: methods) {
            if (Arrays.asList(method.getParameterTypes()).contains(Function1.class)) {
                continue;
            }
            Object[] arguments = new Object[method.getParameterTypes().length];
            int i = 0;
            for (Class type: method.getParameterTypes()) {
                if (type == Long.class || type == long.class) {
                    arguments[i] = (random.nextLong());
                } else if (type == String.class) {
                    arguments[i] = UUID.randomUUID().toString();
                } else if (type == Integer.class || type == int.class) {
                    arguments[i] = random.nextInt();
                } else if (type == Double.class || type == double.class) {
                    arguments[i] = random.nextDouble();
                } else if (type == MediaAd.class) {
                    arguments[i] = new MediaAd();
                } else if (type == MediaAdBreak.class) {
                    arguments[i] = new MediaAdBreak();
                } else if (type == MediaSegment.class) {
                    arguments[i] = new MediaSegment();
                } else if (type == MediaQoS.class) {
                    arguments[i] = new MediaQoS();
                } else {
                    throw new RuntimeException("unknown type: " + type.getName() + "\nmethod: " + method.toString());
                }
                i++;
            }
            if (arguments.length == 0){
                method.invoke(mediaSession);
            } else {
                method.invoke(mediaSession, arguments);
            }
            assertNotNull(method.toString(), mparticle.loggedEvent);

            MediaEvent event = (MediaEvent)mparticle.loggedEvent;

            assertNotNull(event.getMediaContent());
            assertEquals(mediaSession.getTitle(), event.getMediaContent().getName());
            assertEquals(mediaSession.getDuration(), event.getMediaContent().getDuration());
            assertEquals(mediaSession.getMediaContentId(), event.getMediaContent().getContentId());
            assertEquals(mediaSession.getContentType(), event.getMediaContent().getContentType());
            assertEquals(mediaSession.getStreamType(), event.getMediaContent().getStreamType());

            mparticle.loggedEvent = null;
        }
    }

    class MockMParticle extends MParticle {
        BaseEvent loggedEvent = null;

        @Override
        public void logEvent(BaseEvent baseEvent) {
            loggedEvent = baseEvent;
        }
    }
}
