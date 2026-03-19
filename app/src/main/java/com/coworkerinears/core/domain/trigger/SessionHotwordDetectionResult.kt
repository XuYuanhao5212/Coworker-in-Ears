package com.coworkerinears.core.domain.trigger

sealed class SessionHotwordDetectionResult {
    data class Matched(
        val sessionId: String,
        val matchedPhrase: String,
        val transcript: String
    ) : SessionHotwordDetectionResult()

    data class NoMatch(
        val sessionId: String,
        val transcript: String
    ) : SessionHotwordDetectionResult()

    data class SessionNotFound(
        val sessionId: String
    ) : SessionHotwordDetectionResult()

    data class InactiveSession(
        val sessionId: String
    ) : SessionHotwordDetectionResult()
}
