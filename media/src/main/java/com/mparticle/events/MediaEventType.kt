package com.mparticle.events

import com.mparticle.BaseEvent

enum class MediaEventType(private val messageType: String): BaseEvent.MessageType {
    Play("play"),
    Pause("pause"),
    SessionStart("session_start"),
    SessionEnd("session_end"),
    ContentEnd("content_end"),
    SeekStart("seek_start"),
    SeekEnd("seek_end"),
    BufferStart("buffer_start"),
    BufferEnd("buffer_end"),
    UpdatePlayheadPosition("playhead_position"),
    AdClick("ad_click"),
    AdBreakStart("ad_break_start"),
    AdBreakEnd("ad_break_end"),
    AdStart("ad_start"),
    AdEnd("ad_end"),
    AdSkip("ad_skip"),
    SegmentStart("segment_start"),
    SegmentEnd("segment_end"),
    SegmentSkip("segment_skip"),
    UpdateQoS("qos");

    override fun getMessageType() = messageType

}