package com.coworkerinears.core.domain.brand

import com.coworkerinears.core.domain.capability.EarphoneCapability

data class EarphoneBrandProfile(
    val brand: EarphoneBrand,
    val recognitionRules: List<RecognitionRule>,
    val capability: EarphoneCapability,
    val notes: String
)
