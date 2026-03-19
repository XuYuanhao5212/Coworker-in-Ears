package com.coworkerinears.feature.capture

import com.coworkerinears.core.domain.memory.MemoryCaptureMode
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode
import com.coworkerinears.core.domain.trigger.TriggerRequest
import com.coworkerinears.feature.trigger.TriggerCoordinator
import com.coworkerinears.feature.trigger.TriggerValidationResult
import java.time.OffsetDateTime

sealed class TriggeredMemoryCaptureResult {
    data class Success(
        val validation: TriggerValidationResult,
        val capture: StoredMemoryCapture,
    ) : TriggeredMemoryCaptureResult()

    data class Rejected(
        val validation: TriggerValidationResult,
    ) : TriggeredMemoryCaptureResult()

    data object EmptyTranscript : TriggeredMemoryCaptureResult()
}

class TriggeredMemoryCaptureCoordinator(
    private val triggerCoordinator: TriggerCoordinator,
    private val memoryCaptureCoordinator: MemoryCaptureCoordinator,
) {
    fun process(
        request: TriggerRequest,
        rawTranscript: String,
        title: String,
        tags: List<String> = emptyList(),
        participants: List<String> = listOf("user"),
        audioReference: String? = null,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): TriggeredMemoryCaptureResult {
        if (rawTranscript.isBlank()) {
            return TriggeredMemoryCaptureResult.EmptyTranscript
        }

        val validation = triggerCoordinator.validate(request)
        if (!validation.accepted) {
            return TriggeredMemoryCaptureResult.Rejected(validation)
        }

        val capture = memoryCaptureCoordinator.capture(
            MemoryCaptureCommand(
                title = title,
                rawTranscript = rawTranscript,
                tags = tags,
                participants = participants,
                audioReference = audioReference,
                now = now,
                source = request.channel.sourceKey,
                mode = request.channel.captureMode,
                notes = buildCaptureNotes(request, validation),
            ),
        )

        return TriggeredMemoryCaptureResult.Success(
            validation = validation,
            capture = capture,
        )
    }

    private fun buildCaptureNotes(
        request: TriggerRequest,
        validation: TriggerValidationResult,
    ): String {
        val brand = validation.activationPlan.brandResolution.brand?.displayName ?: "App-owned"
        return buildString {
            append("Captured after ")
            append(request.channel.displayName)
            append(" was accepted.")
            append(" Trigger context: ")
            append(brand)
            append(". ")
            append(validation.activationPlan.notes)
        }
    }
}

private val EarphoneTriggerMode.captureMode: MemoryCaptureMode
    get() = when (this) {
        EarphoneTriggerMode.MEDIA_KEY,
        EarphoneTriggerMode.PUSH_TO_TALK,
        -> MemoryCaptureMode.MANUAL

        EarphoneTriggerMode.SESSION_HOTWORD -> MemoryCaptureMode.SESSION
    }

private val EarphoneTriggerMode.sourceKey: String
    get() = when (this) {
        EarphoneTriggerMode.MEDIA_KEY -> "earphone_media_key"
        EarphoneTriggerMode.PUSH_TO_TALK -> "push_to_talk"
        EarphoneTriggerMode.SESSION_HOTWORD -> "session_hotword"
    }
