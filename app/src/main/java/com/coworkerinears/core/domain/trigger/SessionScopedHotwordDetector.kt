package com.coworkerinears.core.domain.trigger

/**
 * Session-scoped hotword contract.
 *
 * This is intentionally not a system hotword API. It is meant to be activated
 * only for an explicit app session and backed by a future on-device speech
 * engine or transcript feeder.
 */
interface SessionScopedHotwordDetector {
    fun openSession(spec: SessionHotwordSpec): SessionHotwordSession

    fun closeSession(sessionId: String)

    fun updatePhrase(sessionId: String, newPhrase: String): SessionHotwordSession?

    fun listActiveSessions(): List<SessionHotwordSession>

    fun feedTranscript(sessionId: String, transcript: String): SessionHotwordDetectionResult
}
