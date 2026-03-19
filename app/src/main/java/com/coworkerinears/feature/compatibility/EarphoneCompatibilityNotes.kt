package com.coworkerinears.feature.compatibility

import com.coworkerinears.core.domain.brand.EarphoneBrand
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode

/**
 * Compatibility doc fragment kept next to code so the MVP stays aligned with
 * implementation constraints.
 */
object EarphoneCompatibilityNotes {
    val supportedBrands: List<EarphoneBrand> = listOf(
        EarphoneBrand.HUAWEI,
        EarphoneBrand.LENOVO,
        EarphoneBrand.OPPO,
        EarphoneBrand.HONOR
    )

    val supportedTriggerModes: List<EarphoneTriggerMode> = listOf(
        EarphoneTriggerMode.MEDIA_KEY,
        EarphoneTriggerMode.PUSH_TO_TALK,
        EarphoneTriggerMode.SESSION_HOTWORD
    )

    val sessionHotwordLimitations = """
        The custom hotword is session-scoped.
        It is user-defined, app-owned, and must be explicitly started by the user.
        We do not promise an OS-level 24/7 always-on hotword.
    """.trimIndent()
}
