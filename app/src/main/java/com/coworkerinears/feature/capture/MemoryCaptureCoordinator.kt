package com.coworkerinears.feature.capture

import com.coworkerinears.core.data.memory.MarkdownMemoryRepository
import com.coworkerinears.core.domain.memory.MemoryActionItem
import com.coworkerinears.core.domain.memory.MemoryCaptureMode
import com.coworkerinears.core.domain.memory.MemoryEntry
import com.coworkerinears.core.domain.memory.MemoryStatus
import com.coworkerinears.core.domain.transcription.TranscriptStructurer
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MemoryCaptureCommand(
    val title: String,
    val rawTranscript: String,
    val tags: List<String> = emptyList(),
    val participants: List<String> = listOf("user"),
    val audioReference: String? = null,
    val now: OffsetDateTime = OffsetDateTime.now(),
    val source: String = "manual_capture",
    val mode: MemoryCaptureMode = MemoryCaptureMode.MANUAL,
    val notes: String = "Generated from a manual trigger flow.",
)

data class StoredMemoryCapture(
    val entry: MemoryEntry,
    val destination: File,
)

class MemoryCaptureCoordinator(
    private val transcriptStructurer: TranscriptStructurer,
    private val memoryRepository: MarkdownMemoryRepository,
) {
    fun capture(command: MemoryCaptureCommand): StoredMemoryCapture {
        val structured = transcriptStructurer.structure(command.rawTranscript)
        val entry = MemoryEntry(
            id = createMemoryId(command.now),
            createdAt = command.now,
            source = command.source,
            mode = command.mode,
            title = command.title.ifBlank { fallbackTitle(structured.summary) },
            tags = command.tags,
            participants = command.participants,
            status = MemoryStatus.DRAFT,
            summary = structured.summary,
            rawTranscript = command.rawTranscript,
            keyEntities = structured.keyEntities,
            actionItems = structured.actionItems.map { MemoryActionItem(text = it) },
            notes = command.notes,
            audioReference = command.audioReference,
        )
        val destination = memoryRepository.save(entry)
        return StoredMemoryCapture(
            entry = entry,
            destination = destination,
        )
    }

    fun captureManualTranscript(
        title: String,
        rawTranscript: String,
        tags: List<String> = emptyList(),
        participants: List<String> = listOf("user"),
        audioReference: String? = null,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): MemoryEntry {
        return capture(
            MemoryCaptureCommand(
                title = title,
                rawTranscript = rawTranscript,
                tags = tags,
                participants = participants,
                audioReference = audioReference,
                now = now,
                source = "manual_capture",
                mode = MemoryCaptureMode.MANUAL,
                notes = "Generated from a manual trigger flow. Session-scoped hotword and earphone triggers can both feed this coordinator.",
            ),
        ).entry
    }

    private fun fallbackTitle(summary: String): String {
        return summary
            .take(42)
            .ifBlank { "Untitled memory" }
    }

    private fun createMemoryId(now: OffsetDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT)
        return "mem_${now.format(formatter)}"
    }
}
