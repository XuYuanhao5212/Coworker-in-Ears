package com.coworkerinears.core.data.resolver

import com.coworkerinears.core.domain.brand.EarphoneBrand

data class EarphoneBrandResolution(
    val brand: EarphoneBrand?,
    val confidence: Double,
    val matchedRuleDescriptions: List<String>,
    val rationale: String
)
