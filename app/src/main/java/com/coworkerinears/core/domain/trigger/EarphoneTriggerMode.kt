package com.coworkerinears.core.domain.trigger

/**
 * Trigger channels supported by the MVP.
 */
enum class EarphoneTriggerMode(
    val displayName: String,
    val description: String,
) {
    MEDIA_KEY(
        displayName = "Earphone media key",
        description = "Uses supported media-button events from the connected earphone.",
    ),
    PUSH_TO_TALK(
        displayName = "Push to talk",
        description = "Lets the user explicitly hold or tap a control before recording starts.",
    ),
    SESSION_HOTWORD(
        displayName = "Session hotword",
        description = "Matches a user-defined phrase only while an explicit app session is active.",
    ),
}
