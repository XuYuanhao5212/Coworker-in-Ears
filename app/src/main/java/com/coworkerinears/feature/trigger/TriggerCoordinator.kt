package com.coworkerinears.feature.trigger

import com.coworkerinears.core.data.catalog.DefaultEarphoneCatalog
import com.coworkerinears.core.data.resolver.EarphoneBrandResolution
import com.coworkerinears.core.data.resolver.EarphoneBrandResolver
import com.coworkerinears.core.domain.capability.EarphoneCapability
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode
import com.coworkerinears.core.domain.trigger.SessionHotwordDetectionResult
import com.coworkerinears.core.domain.trigger.SessionHotwordSession
import com.coworkerinears.core.domain.trigger.SessionHotwordSpec
import com.coworkerinears.core.domain.trigger.SessionScopedHotwordDetector
import com.coworkerinears.core.domain.trigger.TriggerRequest

data class TriggerActivationPlan(
    val brandResolution: EarphoneBrandResolution,
    val capability: EarphoneCapability,
    val preferredModes: List<EarphoneTriggerMode>,
    val notes: String,
)

data class TriggerValidationResult(
    val accepted: Boolean,
    val activationPlan: TriggerActivationPlan,
    val rejectionReason: String? = null,
)

class TriggerCoordinator(
    private val brandResolver: EarphoneBrandResolver,
    private val hotwordDetector: SessionScopedHotwordDetector,
) {
    fun buildActivationPlan(snapshot: EarphoneDeviceSnapshot): TriggerActivationPlan {
        val resolution = brandResolver.resolve(snapshot)
        val capability = resolution.brand?.let { brand ->
            DefaultEarphoneCatalog.profiles
                .firstOrNull { it.brand == brand }
                ?.capability
        } ?: EarphoneCapability(
            supportedTriggerModes = emptySet(),
            notes = "Unknown earphone brand; no brand-specific trigger assumptions are applied.",
        )

        return TriggerActivationPlan(
            brandResolution = resolution,
            capability = capability,
            preferredModes = capability.supportedTriggerModes.toList(),
            notes = if (resolution.brand == null) {
                "Fallback activation plan because the snapshot did not match one of the four supported brands."
            } else {
                "Brand-specific activation plan for ${resolution.brand.displayName}."
            },
        )
    }

    fun validate(request: TriggerRequest): TriggerValidationResult {
        val activationPlan = request.deviceSnapshot
            ?.let(::buildActivationPlan)
            ?: buildAppOwnedPlan(request.channel)

        if (request.channel == EarphoneTriggerMode.SESSION_HOTWORD && request.sessionHotwordSpec == null) {
            return TriggerValidationResult(
                accepted = false,
                activationPlan = activationPlan,
                rejectionReason = "Session hotword triggers require a custom phrase specification.",
            )
        }

        if (request.deviceSnapshot != null && activationPlan.brandResolution.brand == null) {
            return TriggerValidationResult(
                accepted = false,
                activationPlan = activationPlan,
                rejectionReason = "The connected earphone does not match the first-wave supported brands.",
            )
        }

        if (request.channel !in activationPlan.capability.supportedTriggerModes) {
            return TriggerValidationResult(
                accepted = false,
                activationPlan = activationPlan,
                rejectionReason = "The requested trigger channel is not supported for this activation context.",
            )
        }

        return TriggerValidationResult(
            accepted = true,
            activationPlan = activationPlan,
        )
    }

    fun openSessionHotword(spec: SessionHotwordSpec): SessionHotwordSession {
        return hotwordDetector.openSession(spec)
    }

    fun feedSessionHotwordTranscript(
        sessionId: String,
        transcript: String,
    ): SessionHotwordDetectionResult {
        return hotwordDetector.feedTranscript(sessionId, transcript)
    }

    private fun buildAppOwnedPlan(channel: EarphoneTriggerMode): TriggerActivationPlan {
        val capability = when (channel) {
            EarphoneTriggerMode.MEDIA_KEY -> EarphoneCapability(
                supportedTriggerModes = emptySet(),
                notes = "Media key events require a resolved earphone device snapshot.",
            )

            EarphoneTriggerMode.PUSH_TO_TALK,
            EarphoneTriggerMode.SESSION_HOTWORD,
            -> EarphoneCapability(
                supportedTriggerModes = setOf(
                    EarphoneTriggerMode.PUSH_TO_TALK,
                    EarphoneTriggerMode.SESSION_HOTWORD,
                ),
                notes = "App-owned triggers may be started without a resolved earphone snapshot.",
            )
        }

        return TriggerActivationPlan(
            brandResolution = EarphoneBrandResolution(
                brand = null,
                confidence = 0.0,
                matchedRuleDescriptions = emptyList(),
                rationale = "No device snapshot was supplied for this trigger request.",
            ),
            capability = capability,
            preferredModes = capability.supportedTriggerModes.toList(),
            notes = "App-owned activation context without a bound earphone device.",
        )
    }
}
