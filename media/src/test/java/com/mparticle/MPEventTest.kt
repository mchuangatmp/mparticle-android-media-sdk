package com.mparticle

import com.mparticle.media.MediaSession
import com.mparticle.media.events.MediaAttributeKeys
import com.mparticle.media.events.MediaEvent
import com.mparticle.media.events.Options
import com.mparticle.testutils.RandomUtils
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.util.*
import kotlin.math.absoluteValue

class MPEventTest {
    private val random = Random()
    private val randomUtils = RandomUtils()
    val mediaSession = MediaSession.builder {
        contentType = randomUtils.getAlphaNumericString(50)
        streamType = randomUtils.getAlphaNumericString(50)
        duration = random.nextLong().absoluteValue
        mediaContentId = randomUtils.getAlphaNumericString(50)
        title = randomUtils.getAlphaNumericString(50)
    }

    @Test
    fun testOptionsToMPEvent() {
        val options = Options().apply {
            customAttributes = mapOf(
                "key1" to "value1",
                "key2" to "value2"
            )
        }
        val mpEvent = MediaEvent(mediaSession, options = options).toMPEvent()
        assertTrue(mpEvent.customAttributes!!.contains("key1"))
        assertEquals(mpEvent.customAttributes?.get("key1"), "value1")
        assertTrue(mpEvent.customAttributes!!.contains("key2"))
        assertEquals(mpEvent.customAttributes?.get("key2"), "value2")
    }

    @Test
    fun testAllOptionAttributeKeys() {
        val options = Options()
        val fieldsSize = MediaAttributeKeys::class.java.fields.size
        //sanity check
        assertTrue(fieldsSize > 10)
        MediaAttributeKeys::class.java.fields.map {
            it.get(MediaAttributeKeys).toString()
        }.associate {
            it to randomUtils.getAlphaNumericString(10)
        }.let {
            options.customAttributes = it
        }
        val mpEvent = MediaEvent(mediaSession, options = options).toMPEvent()
        assertEquals(fieldsSize, mpEvent.customAttributes?.size?: 0 + mediaSession.attributes.size)
        mpEvent.customAttributes?.forEach {
            assertEquals(options.customAttributes[it.key], it.value)
        }
    }
}