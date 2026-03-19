package com.coworkerinears.app.validation

import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode

enum class ValidationTriggerPreset(
    val mode: EarphoneTriggerMode,
    val displayName: String,
    val requiresDevicePreset: Boolean,
    val requiresHotword: Boolean,
    val pathSummary: String,
) {
    MEDIA_KEY(
        mode = EarphoneTriggerMode.MEDIA_KEY,
        displayName = "Earphone media key",
        requiresDevicePreset = true,
        requiresHotword = false,
        pathSummary = "Simulates a supported earphone sending a media-button trigger.",
    ),
    PUSH_TO_TALK(
        mode = EarphoneTriggerMode.PUSH_TO_TALK,
        displayName = "Push to talk",
        requiresDevicePreset = false,
        requiresHotword = false,
        pathSummary = "Simulates the user explicitly starting a capture without relying on a device event.",
    ),
    SESSION_HOTWORD(
        mode = EarphoneTriggerMode.SESSION_HOTWORD,
        displayName = "Session hotword",
        requiresDevicePreset = false,
        requiresHotword = true,
        pathSummary = "Simulates a custom phrase that is only valid inside the current app session.",
    ),
}
