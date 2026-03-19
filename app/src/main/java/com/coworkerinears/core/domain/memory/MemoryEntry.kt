package com.coworkerinears.core.domain.memory

import java.time.OffsetDateTime

data class MemoryEntry(
    val id: String,
    val createdAt: OffsetDateTime,
    val source: String,
    val mode: MemoryCaptureMode,
    val title: String,
    val tags: List<String> = emptyList(),
    val participants: List<String> = emptyList(),
    val status: MemoryStatus = MemoryStatus.DRAFT,
    val summary: String,
    val rawTranscript: String,
    val keyEntities: List<String> = emptyList(),
    val actionItems: List<MemoryActionItem> = emptyList(),
    val notes: String = "",
    val audioReference: String? = null,
)

enum class MemoryCaptureMode {
    MANUAL,
    SESSION,
    IMPORT,
}

enum class MemoryStatus {
    DRAFT,
    CONFIRMED,
    ARCHIVED,
}

data class MemoryActionItem(
    val text: String,
    val isDone: Boolean = false,
)
