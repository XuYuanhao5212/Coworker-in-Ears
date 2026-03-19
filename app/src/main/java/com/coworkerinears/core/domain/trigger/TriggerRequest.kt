package com.coworkerinears.core.domain.trigger

import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot

/**
 * A trigger request describes how the session should be activated.
 */
data class TriggerRequest(
    val channel: EarphoneTriggerMode,
    val deviceSnapshot: EarphoneDeviceSnapshot? = null,
    val sessionHotwordSpec: SessionHotwordSpec? = null
)
