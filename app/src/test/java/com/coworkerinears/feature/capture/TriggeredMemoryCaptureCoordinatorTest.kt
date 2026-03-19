package com.coworkerinears.feature.capture

import com.coworkerinears.core.data.memory.MarkdownMemoryRepository
import com.coworkerinears.core.data.trigger.InMemorySessionScopedHotwordDetector
import com.coworkerinears.core.data.transcription.HeuristicTranscriptStructurer
import com.coworkerinears.core.data.resolver.DefaultEarphoneBrandResolver
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import com.coworkerinears.core.domain.memory.MemoryCaptureMode
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode
import com.coworkerinears.core.domain.trigger.SessionHotwordSpec
import com.coworkerinears.core.domain.trigger.TriggerRequest
import com.coworkerinears.feature.trigger.TriggerCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.io.path.createTempDirectory

class TriggeredMemoryCaptureCoordinatorTest {
    private fun createCoordinator(rootName: String): TriggeredMemoryCaptureCoordinator {
        val memoryCaptureCoordinator = MemoryCaptureCoordinator(
            transcriptStructurer = HeuristicTranscriptStructurer(),
            memoryRepository = MarkdownMemoryRepository(createTempDirectory(rootName).toFile()),
        )
        val triggerCoordinator = TriggerCoordinator(
            brandResolver = DefaultEarphoneBrandResolver,
            hotwordDetector = InMemorySessionScopedHotwordDetector(),
        )
        return TriggeredMemoryCaptureCoordinator(
            triggerCoordinator = triggerCoordinator,
            memoryCaptureCoordinator = memoryCaptureCoordinator,
        )
    }

    @Test
    fun process_persistsMemoryForSupportedMediaKeyTrigger() {
        val coordinator = createCoordinator("coworker-triggered-capture")

        val result = coordinator.process(
            request = TriggerRequest(
                channel = EarphoneTriggerMode.MEDIA_KEY,
                deviceSnapshot = EarphoneDeviceSnapshot(
                    advertisedName = "OPPO Enco Air",
                    manufacturerName = "OPPO",
                    supportsMediaKeyEvents = true,
                    isCurrentlyConnected = true,
                ),
            ),
            rawTranscript = """
                User: Please follow up with Huawei compatibility testing.
                Assistant: Next step is to export the Markdown memory.
            """.trimIndent(),
            title = "Compatibility follow-up",
        )

        assertTrue(result is TriggeredMemoryCaptureResult.Success)
        val success = result as TriggeredMemoryCaptureResult.Success
        assertTrue(success.capture.destination.exists())
        assertEquals(MemoryCaptureMode.MANUAL, success.capture.entry.mode)
        assertEquals("earphone_media_key", success.capture.entry.source)
    }

    @Test
    fun process_rejectsUnsupportedUnknownBrandRequest() {
        val coordinator = createCoordinator("coworker-trigger-reject")

        val result = coordinator.process(
            request = TriggerRequest(
                channel = EarphoneTriggerMode.MEDIA_KEY,
                deviceSnapshot = EarphoneDeviceSnapshot(
                    advertisedName = "Mystery Pods",
                    manufacturerName = "Unknown",
                    supportsMediaKeyEvents = true,
                    isCurrentlyConnected = true,
                ),
            ),
            rawTranscript = "User: Please capture this anyway.",
            title = "Should not save",
        )

        assertTrue(result is TriggeredMemoryCaptureResult.Rejected)
    }

    @Test
    fun process_marksSessionHotwordCaptureAsSessionMode() {
        val coordinator = createCoordinator("coworker-session-capture")

        val result = coordinator.process(
            request = TriggerRequest(
                channel = EarphoneTriggerMode.SESSION_HOTWORD,
                sessionHotwordSpec = SessionHotwordSpec("Hello Assistant"),
            ),
            rawTranscript = "User: Please follow up with the Lenovo validation list.",
            title = "",
        )

        assertTrue(result is TriggeredMemoryCaptureResult.Success)
        val success = result as TriggeredMemoryCaptureResult.Success
        assertEquals(MemoryCaptureMode.SESSION, success.capture.entry.mode)
        assertEquals("session_hotword", success.capture.entry.source)
        assertTrue(success.capture.entry.title.isNotBlank())
    }
}
