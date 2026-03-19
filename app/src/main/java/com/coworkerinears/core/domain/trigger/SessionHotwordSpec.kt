package com.coworkerinears.core.domain.trigger

import java.util.Locale

/**
 * A custom hotword that only applies inside the current app session.
 *
 * Third-party Android apps cannot promise a system-level 24/7 always-on hotword,
 * so this object and the related APIs are intentionally session-scoped.
 */
data class SessionHotwordSpec(
    val phrase: String,
    val localeTag: String = Locale.CHINA.toLanguageTag(),
    val sensitivity: SessionHotwordSensitivity = SessionHotwordSensitivity.MEDIUM
) {
    val normalizedPhrase: String = normalizePhrase(phrase)

    init {
        require(phrase.isNotBlank()) { "Session hotword phrase cannot be blank." }
    }

    companion object {
        private val NORMALIZE_PATTERN = Regex("[\\p{Punct}\\s]+")

        fun normalizePhrase(value: String): String {
            return value.lowercase(Locale.ROOT)
                .replace(NORMALIZE_PATTERN, "")
        }
    }
}

enum class SessionHotwordSensitivity {
    LOW,
    MEDIUM,
    HIGH
}
