package com.coworkerinears.app.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class LiveMediaKeyOutcome {
    CAPTURED,
    UNSUPPORTED_KEY,
    NO_ACTIVE_ROUTE,
    REJECTED,
}

data class LiveMediaKeyEvent(
    val timeLabel: String,
    val keyLabel: String,
    val deviceName: String? = null,
    val resolvedBrandLabel: String? = null,
    val outcome: LiveMediaKeyOutcome,
    val rejectionReason: String? = null,
    val markdownPath: String? = null,
)

data class LiveMediaKeyDebugState(
    val sessionArmed: Boolean = false,
    val totalEvents: Int = 0,
    val lastEvent: LiveMediaKeyEvent? = null,
)

object LiveMediaKeyDebugStore {
    var state by mutableStateOf(LiveMediaKeyDebugState())
        private set

    fun markSessionArmed(armed: Boolean) {
        state = state.copy(sessionArmed = armed)
    }

    fun record(event: LiveMediaKeyEvent) {
        state = state.copy(
            sessionArmed = true,
            totalEvents = state.totalEvents + 1,
            lastEvent = event,
        )
    }
}