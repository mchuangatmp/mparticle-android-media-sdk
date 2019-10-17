package com.mparticle.events

import com.mparticle.BaseEvent

object MediaEventName {
    val Play = "Play"
    val Pause = "Pause"
    val SessionStart = "Session Start"
    val SessionEnd = "Session End"
    val ContentEnd = "Content End"
    val SeekStart = "Seek Start"
    val SeekEnd = "Seek End"
    val BufferStart = "Buffer Start"
    val BufferEnd = "Buffer End"
    val UpdatePlayheadPosition = "Update Playhead Position"
    val AdClick = "Ad Click"
    val AdBreakStart = "Ad Break Start"
    val AdBreakEnd = "Ad Break End"
    val AdStart = "Ad Start"
    val AdEnd = "Ad End"
    val AdSkip = "Ad Skip"
    val SegmentStart = "Segment Start"
    val SegmentEnd = "Segment End"
    val SegmentSkip = "Segment Skip"
    val UpdateQoS = "Update QoS"
}