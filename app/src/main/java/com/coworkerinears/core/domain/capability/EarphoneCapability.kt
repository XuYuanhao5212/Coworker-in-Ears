package com.coworkerinears.core.domain.capability

import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode

data class EarphoneCapability(
    val supportedTriggerModes: Set<EarphoneTriggerMode>,
    val sessionHotwordIsSessionScoped: Boolean = true,
    val requiresExplicitUserStart: Boolean = true,
    val notes: String = ""
)
