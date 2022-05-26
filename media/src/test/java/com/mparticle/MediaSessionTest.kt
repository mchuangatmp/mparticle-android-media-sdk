package com.mparticle

import com.mparticle.media.MediaSession
import com.mparticle.media.events.*
import com.mparticle.testutils.RandomUtils
import com.mparticle.testutils.TestingUtils
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
                assertTrue(method.toString(), mparticle.loggedEvents.size >= 1)
                assertTrue(mparticle.loggedEvents.any { it is MPEvent && it.eventType == MParticle.EventType.Media })
                mparticle.loggedEvents.clear()
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
            assertTrue(method.toString(), mparticle.loggedEvents.size >= 1)
            assertTrue(mparticle.loggedEvents.any { it is MediaEvent })
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
            assertTrue(method.toString(), mparticle.loggedEvents.size >= 1)

            val event = mparticle.loggedEvents[0] as MediaEvent?

            assertNotNull(event!!.mediaContent)
            assertEquals(mediaSession.title, event.mediaContent.name)
            assertEquals(mediaSession.duration, event.mediaContent.duration)
            assertEquals(mediaSession.mediaContentId, event.mediaContent.contentId)
            assertEquals(mediaSession.contentType, event.mediaContent.contentType)
            assertEquals(mediaSession.streamType, event.mediaContent.streamType)

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
            assertTrue(method.toString(), mparticle.loggedEvents.size >= 2)
            assertTrue(mparticle.loggedEvents.any { it.isPlayheadEvent() })
            if (method.name != "logPlayheadPosition") {
                //if the method is NOT logPlayhead, both logged events should have the proper playhead position
                assertTrue(mparticle.loggedEvents.any { !it.isPlayheadEvent() })
                assertTrue(method.toString(), mparticle.loggedEvents.all { (it as? MediaEvent)?.playheadPosition?.equals(currentPlayhead) ?: true })
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

    @Test
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
            assertEquals(mediaSession.duration, attributes[MediaAttributeKeys.DURATION])
            assertEquals(mediaSession.contentType, attributes[MediaAttributeKeys.CONTENT_TYPE])
            assertEquals(mediaSession.streamType, attributes[MediaAttributeKeys.STREAM_TYPE])
        }
        testSessionMediaContentAttributes()

        mediaSession.logMediaSessionStart()
        attributes = mediaSession.attributes
        assertEquals(6, attributes.size)
        assertNotNull(attributes[MediaAttributeKeys.MEDIA_SESSION_ID])

        mediaSession.logPlayheadPosition(10L)

        attributes = mediaSession.attributes
        assertEquals(7, attributes.size)
        assertEquals(10L, attributes[MediaAttributeKeys.PLAYHEAD_POSITION])

        //make sure no other Media Sessions are started
        afterLogApiInvoked(mediaSession) {}


        attributes = mediaSession.attributes
        assertEquals(7, attributes.size)
        assertNotNull(attributes[MediaAttributeKeys.PLAYHEAD_POSITION])
        assertNotNull(attributes[MediaAttributeKeys.MEDIA_SESSION_ID])
    }

    /**
     * test to make sure that Option's customAttributes and currentPlayPosition are accepted for each "log" method,
     * and applied uniformly
     */
    @Test
    fun optionsMPEventTest() {
        val mparticle = MockMParticle()

        val mediaSession = MediaSession.builder(mparticle) {
            contentType = randomUtils.getAlphaNumericString(50)
            streamType = randomUtils.getAlphaNumericString(50)
            duration = random.nextLong().absoluteValue
            mediaContentId = randomUtils.getAlphaNumericString(50)
            title = randomUtils.getAlphaNumericString(50)
        }
        val options = Options().apply {
            currentPlayheadPosition = random.nextLong()
            customAttributes = mapOf(
                "testKey1" to "testValue1",
                "testKey2" to "testValue2")
        }
        afterLogApiInvoked(mediaSession, options) {
            if (it.name != "logPlayheadPosition") {
                assertTrue(it.toString(), mparticle.loggedEvents.size >= 1)
                val event = mparticle.loggedEvents[0] as MediaEvent
                if (event.customAttributes?.containsKey("testKey1") == false) {
                    println("hi")
                }
                assertTrue(it.toString(), event.customAttributes?.containsKey("testKey1") ?: false)
                assertTrue(it.toString(), event.customAttributes?.get("testKey1") == "testValue1")
                assertTrue(it.toString(), event.customAttributes?.containsKey("testKey2") ?: false)
                assertTrue(it.toString(), event.customAttributes?.get("testKey2") == "testValue2")

                assertEquals(options.currentPlayheadPosition, mediaSession.currentPlayheadPosition)
            }
                mparticle.loggedEvents.clear()
        }
    }

    @Test
    fun qosTest() {
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

        var qos: MediaQoS? = null
        mediaSession.mediaEventListener = { qos = it.qos }

        //test empty QoS, should remain all null
        mediaSession.logQos { }

        assertNotNull(qos)
        assertNull(qos?.startupTime)
        assertNull(qos?.bitRate)
        assertNull(qos?.fps)
        assertNull(qos?.droppedFrames)
        qos = null

        //test single QoS value, all others should remain null since they've never been set
        val singleTestQos = MediaQoS(fps = 5)
        mediaSession.logQos(singleTestQos)
        assertNotNull(qos)
        assertNull(qos?.startupTime)
        assertNull(qos?.bitRate)
        assertEquals(qos?.fps, singleTestQos.fps)
        assertNull(qos?.droppedFrames)

        //test update whole object, overwrite everything
        val fullTestQos = MediaQoS(startupTime = 1, bitRate = 2, fps = 3, droppedFrames = 4)
        mediaSession.logQos(fullTestQos)

        assertNotNull(qos)
        assertEquals(qos?.startupTime, fullTestQos.startupTime)
        assertEquals(qos?.bitRate, fullTestQos.bitRate)
        assertEquals(qos?.fps, fullTestQos.fps)
        assertEquals(qos?.droppedFrames, fullTestQos.droppedFrames)
        qos = null

        //test empty object when all fields are already set, should send all existing values
        mediaSession.logQos { }

        assertNotNull(qos)
        assertEquals(qos?.startupTime, fullTestQos.startupTime)
        assertEquals(qos?.bitRate, fullTestQos.bitRate)
        assertEquals(qos?.fps, fullTestQos.fps)
        assertEquals(qos?.droppedFrames, fullTestQos.droppedFrames)
        qos = null

        //test partial object when all fields are already set, should update fields included and send existing values for the rest
        val partialTestQos = MediaQoS(startupTime = 5, fps = 6)
        mediaSession.logQos(partialTestQos)
        assertNotNull(qos)
        assertEquals(qos?.startupTime, partialTestQos.startupTime)
        assertEquals(qos?.bitRate, fullTestQos.bitRate)
        assertEquals(qos?.fps, partialTestQos.fps)
        assertEquals(qos?.droppedFrames, fullTestQos.droppedFrames)
    }

    @Test
    fun `test MPEvents built from MediaSession have proper EventType`() {
        val mparticle = MockMParticle()
        val mediaSessionMPEvent = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId ="123"
            duration =1000
            streamType = StreamType.LIVE_STEAM
            contentType = ContentType.VIDEO

            logMediaEvents = random.nextBoolean()
            logMPEvents = random.nextBoolean()
        }.buildMPEvent("some name", null)
        assertEquals(MParticle.EventType.Media, mediaSessionMPEvent.eventType)

    }

    private fun afterLogApiInvoked(mediaSession: MediaSession, options: Options? = null, onEvent: (Method) -> Unit) {
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
                } else if (type == Options::class.java) {
                    arguments[i] = options
                } else if (type == Map::class.java) {
                    arguments[i] = RandomUtils.getInstance().getRandomAttributes(6, false)
                } else {
                    throw RuntimeException("unknown type: " + type.name + "\nmethod: " + method.toString())
                }
                i++
            }
            try {
                if (arguments.size == 0) {
                    method.invoke(mediaSession)
                } else {
                    method.invoke(mediaSession, *arguments)
                }
            } catch (e: Exception) {
                throw e;
            }
            onEvent(method)
        }
    }

    @Test
    fun testLogAd() {
        val mparticle = MockMParticle()
        val mediaSession = MediaSession.builder(mparticle) {
            title = "hello"
            mediaContentId ="123"
            duration =1000
        }

        val events = mutableListOf<MediaEvent>()
        mediaSession.mediaEventListener = { event ->
            events.add(event)
        }

        //test with java interface methods
        mediaSession.logAdBreakStart(MediaAdBreak().apply { id = "adbreak 1" })
        mediaSession.logAdStart(MediaAd().apply { id = "ad 1" })
        mediaSession.logAdClick()
        mediaSession.logAdEnd()
        mediaSession.logAdBreakEnd()

        //test with kotlin interface methods (overloads)
        mediaSession.logAdBreakStart {
            id = "adbreak 2"
        }
        mediaSession.logAdStart {
            id = "ad 2"
        }
        mediaSession.logAdClick()
        mediaSession.logAdEnd()
        mediaSession.logAdBreakEnd()

        assertEquals(10, events.size)
        events.forEach {
            it.mediaContent.apply {
                assertEquals("hello", name)
                assertEquals("123", contentId)
                assertEquals(1000L, duration)
            }
        }
        events[0].assertTrue { it.eventName == MediaEventName.AD_BREAK_START && it.adBreak!!.id == "adbreak 1" }
        events[1].assertTrue { it.eventName == MediaEventName.AD_START && it.mediaAd!!.id == "ad 1" }
        events[2].assertTrue { it.eventName == MediaEventName.AD_CLICK && it.mediaAd!!.id == "ad 1" }
        events[3].assertTrue { it.eventName == MediaEventName.AD_END }
        events[4].assertTrue { it.eventName == MediaEventName.AD_BREAK_END }
        events[5].assertTrue { it.eventName == MediaEventName.AD_BREAK_START && it.adBreak!!.id == "adbreak 2" }
        events[6].assertTrue { it.eventName == MediaEventName.AD_START && it.mediaAd!!.id == "ad 2" }
        events[7].assertTrue { it.eventName == MediaEventName.AD_CLICK && it.mediaAd!!.id == "ad 2" }
        events[8].assertTrue { it.eventName == MediaEventName.AD_END }
        events[9].assertTrue { it.eventName == MediaEventName.AD_BREAK_END}
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

fun <T: Any> T.assertTrue(assertion: (T) -> Boolean) {
    assertion(this)
}