package com.coworkerinears.core.domain.trigger

data class SessionHotwordSession(
    val sessionId: String,
    val spec: SessionHotwordSpec,
    val startedAtMillis: Long,
    val state: SessionHotwordState = SessionHotwordState.ACTIVE
)

enum class SessionHotwordState {
    ACTIVE,
    STOPPED
}
