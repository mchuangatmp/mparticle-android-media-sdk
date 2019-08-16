package com.mparticle.events

enum class MediaEventType(val type: Int) {
    Play(23),
    Pause(24),
    SessionStart(30),
    SessionEnd(31),
    SeekStart(32),
    SeekEnd(33),
    BufferStart(34),
    BufferEnd(35),
    UpdatePlayheadPosition(36),
    AdClick(37),
    AdBreakStart(38),
    AdBreakEnd(39),
    AdStart(40),
    AdEnd(41),
    AdSkip(42),
    SegmentStart(43),
    SegmentEnd(44),
    SegmentSkip(45),
    UpdateQoS(46)
}