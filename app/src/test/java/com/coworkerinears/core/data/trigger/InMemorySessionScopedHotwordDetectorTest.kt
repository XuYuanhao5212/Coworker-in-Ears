package com.coworkerinears.core.data.trigger

import com.coworkerinears.core.domain.trigger.SessionHotwordDetectionResult
import com.coworkerinears.core.domain.trigger.SessionHotwordSpec
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySessionScopedHotwordDetectorTest {
    @Test
    fun feedTranscript_matchesNormalizedPhraseInsideActiveSession() {
        val detector = InMemorySessionScopedHotwordDetector()
        val session = detector.openSession(SessionHotwordSpec(phrase = "Hey Coworker"))

        val result = detector.feedTranscript(
            sessionId = session.sessionId,
            transcript = "Can you help me, hey coworker?",
        )

        assertTrue(result is SessionHotwordDetectionResult.Matched)
    }
}
