package com.coworkerinears.app.validation

import com.coworkerinears.app.AppLanguage
import com.coworkerinears.app.localizedDisplayName
import com.coworkerinears.core.data.memory.MarkdownMemoryRepository
import com.coworkerinears.core.data.resolver.DefaultEarphoneBrandResolver
import com.coworkerinears.core.data.transcription.HeuristicTranscriptStructurer
import com.coworkerinears.core.data.trigger.InMemorySessionScopedHotwordDetector
import com.coworkerinears.core.domain.trigger.SessionHotwordSpec
import com.coworkerinears.core.domain.trigger.TriggerRequest
import com.coworkerinears.feature.capture.MemoryCaptureCoordinator
import com.coworkerinears.feature.capture.TriggeredMemoryCaptureCoordinator
import com.coworkerinears.feature.capture.TriggeredMemoryCaptureResult
import com.coworkerinears.feature.trigger.TriggerCoordinator
import java.io.File
import java.time.OffsetDateTime

data class ValidationCaptureDraft(
    val triggerPreset: ValidationTriggerPreset,
    val devicePreset: ValidationDevicePreset? = null,
    val hotword: String = "Hey Coworker",
    val title: String = "",
    val transcript: String = "",
    val tags: List<String> = emptyList(),
    val participants: List<String> = listOf("user"),
)

data class ValidationArtifactPreview(
    val path: String,
    val preview: String,
    val lastModifiedAtMillis: Long,
)

data class ValidationExecutionReport(
    val accepted: Boolean,
    val rejectionReason: String? = null,
    val resolvedBrand: String? = null,
    val markdownPath: String? = null,
    val markdownBody: String? = null,
    val notes: String,
)

class ValidationHarness(
    private val rootDirectory: File,
) {
    private val repository = MarkdownMemoryRepository(rootDirectory)
    private val triggerCoordinator = TriggerCoordinator(
        brandResolver = DefaultEarphoneBrandResolver,
        hotwordDetector = InMemorySessionScopedHotwordDetector(),
    )
    private val memoryCaptureCoordinator = MemoryCaptureCoordinator(
        transcriptStructurer = HeuristicTranscriptStructurer(),
        memoryRepository = repository,
    )
    private val triggeredCoordinator = TriggeredMemoryCaptureCoordinator(
        triggerCoordinator = triggerCoordinator,
        memoryCaptureCoordinator = memoryCaptureCoordinator,
    )

    fun permissionGuide(
        recordAudioGranted: Boolean,
        bluetoothConnectGranted: Boolean,
        language: AppLanguage,
    ): ValidationPermissionGuide {
        val canRunEarphoneValidation = recordAudioGranted && bluetoothConnectGranted
        val steps = buildList {
            if (!recordAudioGranted) {
                add(
                    if (language == AppLanguage.ZH) {
                        "先授予麦克风权限，再尝试真实录音验证。"
                    } else {
                        "Grant microphone permission before trying live recording validation."
                    },
                )
            }
            if (!bluetoothConnectGranted) {
                add(
                    if (language == AppLanguage.ZH) {
                        "授予蓝牙连接权限，这样应用才能读取耳机设备快照。"
                    } else {
                        "Grant Bluetooth connect permission so the app can inspect the earphone device snapshot."
                    },
                )
            }
            add(
                if (canRunEarphoneValidation) {
                    if (language == AppLanguage.ZH) {
                        "权限已经具备。等耳机到手后，可以从模拟模式切换到真实硬件验证。"
                    } else {
                        "Permissions look good. You can move from simulated triggers to live earphone validation when hardware is available."
                    }
                } else {
                    if (language == AppLanguage.ZH) {
                        "在硬件和权限到位前，仍然可以先用模拟模式、按住说话和占位 transcript 流程。"
                    } else {
                        "You can still use simulation mode, push-to-talk, and placeholder transcript flows without the full hardware path."
                    }
                },
            )
        }

        return ValidationPermissionGuide(
            recordAudioGranted = recordAudioGranted,
            bluetoothConnectGranted = bluetoothConnectGranted,
            canRunEarphoneValidation = canRunEarphoneValidation,
            statusHeadline = if (canRunEarphoneValidation) {
                if (language == AppLanguage.ZH) "可以进入硬件验证" else "Ready for hardware validation"
            } else {
                if (language == AppLanguage.ZH) "硬件或权限未就绪，先使用模拟模式" else "Use simulation mode until permissions and hardware are ready"
            },
            nextSteps = steps,
        )
    }

    fun buildRequest(draft: ValidationCaptureDraft): TriggerRequest {
        return TriggerRequest(
            channel = draft.triggerPreset.mode,
            deviceSnapshot = if (draft.triggerPreset.requiresDevicePreset) {
                draft.devicePreset?.toSnapshot()
            } else {
                null
            },
            sessionHotwordSpec = if (draft.triggerPreset.requiresHotword) {
                SessionHotwordSpec(phrase = draft.hotword)
            } else {
                null
            },
        )
    }

    fun buildPlaceholderTranscript(
        triggerPreset: ValidationTriggerPreset,
        devicePreset: ValidationDevicePreset?,
        language: AppLanguage,
    ): String {
        val deviceLine = devicePreset?.localizedDisplayName(language) ?: if (language == AppLanguage.ZH) {
            "应用内触发"
        } else {
            "App-owned trigger"
        }
        return if (language == AppLanguage.ZH) {
            """
                用户：请用 $deviceLine 帮我把这个想法记下来。
                助手：${triggerPreset.localizedDisplayName(language)} 这条路径正在做验证，在真实耳机到手前先完成模拟联调。
                用户：请把它写成 Markdown 记忆，并把行动项保留下来。
            """.trimIndent()
        } else {
            """
                User: Capture this idea with $deviceLine.
                Assistant: The ${triggerPreset.localizedDisplayName(language)} path is being validated before real earphone hardware is available.
                User: Please turn this into Markdown memory and keep the action items visible.
            """.trimIndent()
        }
    }

    fun execute(
        draft: ValidationCaptureDraft,
        language: AppLanguage,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): ValidationExecutionReport {
        val transcript = draft.transcript.ifBlank {
            buildPlaceholderTranscript(
                triggerPreset = draft.triggerPreset,
                devicePreset = draft.devicePreset,
                language = language,
            )
        }

        val result = triggeredCoordinator.process(
            request = buildRequest(draft),
            rawTranscript = transcript,
            title = draft.title,
            tags = draft.tags,
            participants = draft.participants,
            audioReference = if (draft.triggerPreset == ValidationTriggerPreset.PUSH_TO_TALK) {
                "audio/simulated_capture.enc"
            } else {
                null
            },
            now = now,
        )

        return when (result) {
            is TriggeredMemoryCaptureResult.EmptyTranscript -> ValidationExecutionReport(
                accepted = false,
                rejectionReason = "Transcript cannot be empty.",
                notes = "Simulation did not run because there was no transcript content.",
            )

            is TriggeredMemoryCaptureResult.Rejected -> ValidationExecutionReport(
                accepted = false,
                rejectionReason = result.validation.rejectionReason,
                resolvedBrand = result.validation.activationPlan.brandResolution.brand?.displayName,
                notes = result.validation.activationPlan.notes,
            )

            is TriggeredMemoryCaptureResult.Success -> ValidationExecutionReport(
                accepted = true,
                resolvedBrand = result.validation.activationPlan.brandResolution.brand?.displayName,
                markdownPath = result.capture.destination.absolutePath,
                markdownBody = repository.readFile(result.capture.destination),
                notes = result.validation.activationPlan.notes,
            )
        }
    }

    fun recentArtifacts(limit: Int = 5): List<ValidationArtifactPreview> {
        return repository.listMarkdownFiles(limit).map { file ->
            ValidationArtifactPreview(
                path = file.absolutePath,
                preview = repository.readFile(file)
                    .lineSequence()
                    .take(8)
                    .joinToString(separator = "\n"),
                lastModifiedAtMillis = file.lastModified(),
            )
        }
    }
}
