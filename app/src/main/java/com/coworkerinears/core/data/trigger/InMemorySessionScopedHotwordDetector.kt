package com.coworkerinears.core.data.trigger

import com.coworkerinears.core.domain.trigger.SessionHotwordDetectionResult
import com.coworkerinears.core.domain.trigger.SessionHotwordSession
import com.coworkerinears.core.domain.trigger.SessionHotwordSpec
import com.coworkerinears.core.domain.trigger.SessionHotwordState
import com.coworkerinears.core.domain.trigger.SessionScopedHotwordDetector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Default session-scoped hotword skeleton.
 *
 * This implementation is intentionally lightweight: it keeps the session state
 * in memory and performs transcript-level matching only. A real audio engine
 * can be swapped in later without changing the contract.
 */
class InMemorySessionScopedHotwordDetector : SessionScopedHotwordDetector {
    private data class ActiveSession(
        val session: SessionHotwordSession
    )

    private val activeSessions = ConcurrentHashMap<String, ActiveSession>()

    override fun openSession(spec: SessionHotwordSpec): SessionHotwordSession {
        val session = SessionHotwordSession(
            sessionId = UUID.randomUUID().toString(),
            spec = spec,
            startedAtMillis = System.currentTimeMillis(),
            state = SessionHotwordState.ACTIVE
        )
        activeSessions[session.sessionId] = ActiveSession(session)
        return session
    }

    override fun closeSession(sessionId: String) {
        activeSessions.remove(sessionId)
    }

    override fun updatePhrase(sessionId: String, newPhrase: String): SessionHotwordSession? {
        val current = activeSessions[sessionId]?.session ?: return null
        val updated = current.copy(
            spec = current.spec.copy(phrase = newPhrase),
            state = SessionHotwordState.ACTIVE
        )
        activeSessions[sessionId] = ActiveSession(updated)
        return updated
    }

    override fun listActiveSessions(): List<SessionHotwordSession> {
        return activeSessions.values.map { it.session }
    }

    override fun feedTranscript(sessionId: String, transcript: String): SessionHotwordDetectionResult {
        val session = activeSessions[sessionId]?.session
            ?: return SessionHotwordDetectionResult.SessionNotFound(sessionId)

        if (session.state != SessionHotwordState.ACTIVE) {
            return SessionHotwordDetectionResult.InactiveSession(sessionId)
        }

        val normalizedTranscript = SessionHotwordSpec.normalizePhrase(transcript)
        val matchedPhrase = session.spec.normalizedPhrase
        return if (normalizedTranscript.contains(matchedPhrase)) {
            SessionHotwordDetectionResult.Matched(
                sessionId = sessionId,
                matchedPhrase = session.spec.phrase,
                transcript = transcript
            )
        } else {
            SessionHotwordDetectionResult.NoMatch(
                sessionId = sessionId,
                transcript = transcript
            )
        }
    }
}
