package com.mparticle

import com.mparticle.media.MediaSession
import com.mparticle.media.events.*
import com.mparticle.testutils.RandomUtils
import junit.framework.Assert.*
import org.junit.Test
import java.lang.reflect.Method
import java.util.*
import kotlin.math.absoluteValue

class MediaSessionTest  {
    private val random = Random()
    private val randomUtils = RandomUtils()

    @Test
    fun testCallbackInvoke() {
        val mparticle = MockMParticle()

        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId ="123"
            duration =1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO

            logMediaEvents = random.nextBoolean()
            logMPEvents = random.nextBoolean()
        }

        var lastMediaEvent: MediaEvent? = null
        mediaSession.mediaEventListener = {
            assertNull(lastMediaEvent)
            lastMediaEvent = it
        }

        afterLogApiInvoked(mediaSession) { method ->
            assertNotNull(lastMediaEvent)
            lastMediaEvent = null
        }
    }

    @Test
    fun testLogOnlyMPEvents() {
        val mparticle = MockMParticle()

        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId ="123"
            duration =1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO

            logMediaEvents = false
            logMPEvents = true
        }

        afterLogApiInvoked(mediaSession) { method ->
            if (method.name != "logPlayheadPosition") {
                assertEquals(method.toString(), 1, mparticle.loggedEvents.size)
                mparticle.loggedEvents.first { it is MPEvent }
                mparticle.loggedEvents.clear()
            } else {
                assertEquals(method.toString(), 0, mparticle.loggedEvents.size)
            }
        }
    }

    @Test
    fun testLogOnlyMedia() {
        val mparticle = MockMParticle()

        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId ="123"
            duration =1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO

            logMediaEvents = true
            logMPEvents = false
        }

        afterLogApiInvoked(mediaSession) { method ->
            assertEquals(method.toString(), 1, mparticle.loggedEvents.size)
            mparticle.loggedEvents.first { it is MediaEvent }
            mparticle.loggedEvents.clear()
        }


    }

    @Test
    fun testAllMediaEventsHaveMediaContent() {
        val mparticle = MockMParticle()


        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId = "123"
            duration = 1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO
        }



        afterLogApiInvoked (mediaSession) { method ->
            assertEquals(method.toString(), 1, mparticle.loggedEvents.size)

            val event = mparticle.loggedEvents[0] as MediaEvent?

            assertNotNull(event!!.mediaContent)
            assertEquals(mediaSession.title, event!!.mediaContent.name)
            assertEquals(mediaSession.duration, event!!.mediaContent.duration)
            assertEquals(mediaSession.mediaContentId, event!!.mediaContent.contentId)
            assertEquals(mediaSession.contentType, event!!.mediaContent.contentType)
            assertEquals(mediaSession.streamType, event!!.mediaContent.streamType)

            mparticle.loggedEvents.clear()
        }
    }

    @Test
    fun testAllMediaEventsHavePlayheadPosition() {
        val mparticle = MockMParticle()


        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId = "123"
            duration = 1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO
        }


        var currentPlayhead = 0L

        fun updateRandomPlayhead() {
            currentPlayhead = random.nextLong().absoluteValue
            mediaSession.logPlayheadPosition(currentPlayhead)
        }

        updateRandomPlayhead()

        afterLogApiInvoked (mediaSession) { method ->
            assertEquals(method.toString(), 2, mparticle.loggedEvents.size)
            assertTrue(mparticle.loggedEvents.any { it.isPlayheadEvent() })
            if (method.name != "logPlayheadPosition") {
                //if the method is NOT logPlayhead, both logged events should have the proper playhead position
                assertTrue(mparticle.loggedEvents.any { !it.isPlayheadEvent() })
                assertTrue(method.toString(), mparticle.loggedEvents.all { (it as MediaEvent).playheadPosition == currentPlayhead })
            } else {
                //if the method is logPlayhead, just make sure the first event (the one called from this method) has the proper playhead
                // ..the one called from afterLogApiInvoked will have a random value
                assertTrue(method.toString(), (mparticle.loggedEvents[0] as MediaEvent).playheadPosition == currentPlayhead)
            }
            mparticle.loggedEvents.clear()
            updateRandomPlayhead()
        }
    }

    @Test
    fun reallyExplicitMakeSureLogPlayheadEventsAreNeverLoggedAsMPEventTest() {
        val mparticle = MockMParticle()

        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId ="123"
            duration =1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO

            logMediaEvents = false
            logMPEvents = true
        }

        mediaSession.logPlayheadPosition(1L)
        assertEquals(0, mparticle.loggedEvents.size)

        mediaSession.logPlayheadPosition(random.nextLong())
        assertEquals(0, mparticle.loggedEvents.size)

        mediaSession.logPlayheadPosition(random.nextLong())
        assertEquals(0, mparticle.loggedEvents.size)

        mediaSession.logPlayheadPosition(random.nextLong())
        assertEquals(0, mparticle.loggedEvents.size)

        mediaSession.logPlayheadPosition(random.nextLong())
        assertEquals(0, mparticle.loggedEvents.size)
    }

    fun attributesTest() {
        val mediaSession = MediaSession.builder {
            contentType = randomUtils.getAlphaNumericString(50)
            streamType = randomUtils.getAlphaNumericString(50)
            duration = random.nextLong().absoluteValue
            mediaContentId = randomUtils.getAlphaNumericString(50)
            title = randomUtils.getAlphaNumericString(50)
        }

        var attributes = mediaSession.attributes
        assertEquals(5, attributes.size)

        fun testSessionMediaContentAttributes() {
            assertEquals(mediaSession.mediaContentId, attributes[MediaAttributeKeys.CONTENT_ID])
            assertEquals(mediaSession.title, attributes[MediaAttributeKeys.TITLE])
            assertEquals(mediaSession.duration.toString(), attributes[MediaAttributeKeys.DURATION])
            assertEquals(mediaSession.contentType, attributes[MediaAttributeKeys.CONTENT_TYPE])
            assertEquals(mediaSession.streamType, attributes[MediaAttributeKeys.STREAM_TYPE])
        }
        testSessionMediaContentAttributes()

        mediaSession.logPlayheadPosition(10L)

        attributes = mediaSession.attributes
        assertEquals(6, attributes.size)
        assertEquals(10.toString(), attributes[MediaAttributeKeys.PLAYHEAD_POSITION])

        //make sure no other Media Sessions are started
        afterLogApiInvoked(mediaSession) {}

        attributes = mediaSession.attributes
        assertEquals(6, attributes.size)
        assertNotNull(attributes[MediaAttributeKeys.PLAYHEAD_POSITION])

    }

    fun afterLogApiInvoked(mediaSession: MediaSession, onEvent: (Method) -> Unit) {
        val methods = ArrayList<Method>()
        for (method in MediaSession::class.java.methods) {
            if (method.name != "logCustomEvent" && method.name.startsWith("log") && !method.name.endsWith("\$default")) {
                methods.add(method)
            }
        }

        for (method in methods) {
            if (Arrays.asList(*method.parameterTypes).contains(Function1::class.java)) {
                continue
            }
            val arguments = arrayOfNulls<Any>(method.parameterTypes.size)
            var i = 0
            for (type in method.parameterTypes) {
                if (type == Long::class.java || type == Long::class.javaPrimitiveType) {
                    arguments[i] = random.nextLong()
                } else if (type == String::class.java) {
                    arguments[i] = UUID.randomUUID().toString()
                } else if (type == Int::class.java || type == Int::class.javaPrimitiveType) {
                    arguments[i] = random.nextInt()
                } else if (type == Double::class.java || type == Double::class.javaPrimitiveType) {
                    arguments[i] = random.nextDouble()
                } else if (type == MediaAd::class.java) {
                    arguments[i] = MediaAd()
                } else if (type == MediaAdBreak::class.java) {
                    arguments[i] = MediaAdBreak()
                } else if (type == MediaSegment::class.java) {
                    arguments[i] = MediaSegment()
                } else if (type == MediaQoS::class.java) {
                    arguments[i] = MediaQoS()
                } else {
                    throw RuntimeException("unknown type: " + type.name + "\nmethod: " + method.toString())
                }
                i++
            }
            if (arguments.size == 0) {
                method.invoke(mediaSession)
            } else {
                method.invoke(mediaSession, *arguments)
            }
            onEvent(method)
        }
    }

    fun BaseEvent.isPlayheadEvent(): Boolean {
        return if (this is MediaEvent) {
            eventName == MediaEventName.UPDATE_PLAYHEAD_POSITION
        } else {
            false
        }
    }

    inner class MockMParticle : MParticle() {
        var loggedEvents: MutableList<BaseEvent> = ArrayList()

        override fun logEvent(baseEvent: BaseEvent) {
            loggedEvents.add(baseEvent)
        }
    }
}