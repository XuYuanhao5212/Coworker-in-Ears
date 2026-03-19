package com.coworkerinears.core.domain.memory

import java.time.OffsetDateTime
import java.time.ZoneOffset

object MemorySamples {
    fun kickoffEntry(): MemoryEntry {
        return MemoryEntry(
            id = "mem_20260318_001",
            createdAt = OffsetDateTime.of(2026, 3, 18, 9, 30, 0, 0, ZoneOffset.ofHours(8)),
            source = "earbud_session",
            mode = MemoryCaptureMode.MANUAL,
            title = "MVP kickoff memo",
            tags = listOf("mvp", "android", "memory"),
            participants = listOf("user", "assistant"),
            status = MemoryStatus.DRAFT,
            summary = "Aligned the first Android MVP around four earphone brands, session-scoped hotwords, and Markdown memory.",
            rawTranscript = """
                User: First release stays on Android.
                Assistant: We will support Huawei, Lenovo, OPPO, and Honor first.
                User: Keep memory in Markdown so it is readable and easy to audit.
            """.trimIndent(),
            keyEntities = listOf("Android", "Huawei", "Lenovo", "OPPO", "Honor", "Markdown"),
            actionItems = listOf(
                MemoryActionItem("Implement brand compatibility heuristics"),
                MemoryActionItem("Add session-scoped custom phrase support"),
                MemoryActionItem("Write Markdown memory serializer"),
            ),
            notes = "The wake phrase is intentionally scoped to an explicit user session, not a system-level always-on assistant.",
            audioReference = "audio/mem_20260318_001.enc",
        )
    }
}
