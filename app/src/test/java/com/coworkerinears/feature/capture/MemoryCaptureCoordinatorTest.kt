package com.coworkerinears.feature.capture

import com.coworkerinears.core.data.memory.MarkdownMemoryRepository
import com.coworkerinears.core.data.memory.MemoryMarkdownSerializer
import com.coworkerinears.core.data.transcription.HeuristicTranscriptStructurer
import com.coworkerinears.core.domain.memory.MemoryCaptureMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class MemoryCaptureCoordinatorTest {
    @Test
    fun captureManualTranscript_writesMarkdownMemory() {
        val root = createTempDirectory("coworker-memory-test").toFile()
        val coordinator = MemoryCaptureCoordinator(
            transcriptStructurer = HeuristicTranscriptStructurer(),
            memoryRepository = MarkdownMemoryRepository(root),
        )

        val entry = coordinator.captureManualTranscript(
            title = "Launch checklist",
            rawTranscript = """
                User: We need to validate Huawei and Honor earphones first.
                Assistant: Please follow up on Markdown export next.
            """.trimIndent(),
        )

        val savedFile = File(root, MemoryMarkdownSerializer.suggestedRelativePath(entry))
        assertTrue(savedFile.exists())
        assertTrue(savedFile.readText().contains("## Summary"))
        assertEquals(MemoryCaptureMode.MANUAL, entry.mode)
    }

    @Test
    fun capture_returnsDestinationAndFallsBackToSummaryTitle() {
        val root = createTempDirectory("coworker-memory-command").toFile()
        val coordinator = MemoryCaptureCoordinator(
            transcriptStructurer = HeuristicTranscriptStructurer(),
            memoryRepository = MarkdownMemoryRepository(root),
        )

        val capture = coordinator.capture(
            MemoryCaptureCommand(
                title = "",
                rawTranscript = """
                    User: Please follow up with Lenovo validation.
                    Assistant: Next step is to draft the compatibility note.
                """.trimIndent(),
                mode = MemoryCaptureMode.SESSION,
                source = "session_hotword",
            ),
        )

        assertTrue(capture.destination.exists())
        assertEquals(MemoryCaptureMode.SESSION, capture.entry.mode)
        assertTrue(capture.entry.title.isNotBlank())
    }
}
