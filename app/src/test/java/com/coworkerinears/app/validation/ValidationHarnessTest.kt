package com.coworkerinears.app.validation

import com.coworkerinears.app.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import kotlin.io.path.createTempDirectory

class ValidationHarnessTest {
    @Test
    fun execute_savesMarkdownForSupportedEarphoneSimulation() {
        val harness = ValidationHarness(createTempDirectory("coworker-harness").toFile())

        val report = harness.execute(
            draft = ValidationCaptureDraft(
                triggerPreset = ValidationTriggerPreset.MEDIA_KEY,
                devicePreset = ValidationDevicePreset.HUAWEI,
                title = "Hardware smoke test",
                transcript = "",
                tags = listOf("smoke"),
            ),
            language = AppLanguage.EN,
            now = OffsetDateTime.parse("2026-03-19T10:15:30+08:00"),
        )

        assertTrue(report.accepted)
        assertNotNull(report.markdownPath)
        assertTrue(report.markdownBody!!.contains("Hardware smoke test"))
        assertTrue(report.markdownBody!!.contains("## Action Items"))
    }

    @Test
    fun execute_rejectsUnknownBrandMediaKeySimulation() {
        val harness = ValidationHarness(createTempDirectory("coworker-harness-reject").toFile())

        val report = harness.execute(
            draft = ValidationCaptureDraft(
                triggerPreset = ValidationTriggerPreset.MEDIA_KEY,
                devicePreset = ValidationDevicePreset.UNKNOWN,
            ),
            language = AppLanguage.EN,
        )

        assertFalse(report.accepted)
        assertTrue(report.rejectionReason!!.contains("first-wave supported brands"))
    }

    @Test
    fun permissionGuide_allowsSimulationWithoutFullHardwarePermissions() {
        val harness = ValidationHarness(createTempDirectory("coworker-harness-perms").toFile())

        val guide = harness.permissionGuide(
            recordAudioGranted = false,
            bluetoothConnectGranted = false,
            language = AppLanguage.EN,
        )

        assertFalse(guide.canRunEarphoneValidation)
        assertEquals(
            "Use simulation mode until permissions and hardware are ready",
            guide.statusHeadline,
        )
        assertTrue(guide.nextSteps.any { it.contains("simulation", ignoreCase = true) })
    }

    @Test
    fun execute_pushToTalkSimulationAddsAudioReference() {
        val harness = ValidationHarness(createTempDirectory("coworker-harness-audio").toFile())

        val report = harness.execute(
            draft = ValidationCaptureDraft(
                triggerPreset = ValidationTriggerPreset.PUSH_TO_TALK,
                title = "PTT validation",
                transcript = "",
            ),
            language = AppLanguage.EN,
        )

        assertTrue(report.accepted)
        assertTrue(report.markdownBody!!.contains("audio_reference: \"audio/simulated_capture.enc\""))
        assertTrue(report.markdownBody!!.contains("PTT validation"))
    }

    @Test
    fun recentArtifacts_returnsNewestMarkdownFirst() {
        val harness = ValidationHarness(createTempDirectory("coworker-harness-history").toFile())

        harness.execute(
            draft = ValidationCaptureDraft(
                triggerPreset = ValidationTriggerPreset.PUSH_TO_TALK,
                title = "Older note",
            ),
            language = AppLanguage.EN,
            now = OffsetDateTime.parse("2026-03-19T09:00:00+08:00"),
        )
        harness.execute(
            draft = ValidationCaptureDraft(
                triggerPreset = ValidationTriggerPreset.SESSION_HOTWORD,
                title = "Newer note",
            ),
            language = AppLanguage.EN,
            now = OffsetDateTime.parse("2026-03-19T11:00:00+08:00"),
        )

        val artifacts = harness.recentArtifacts()

        assertEquals(2, artifacts.size)
        assertTrue(artifacts.first().path.contains("mem_20260319_110000"))
    }
}
