package com.coworkerinears.core.data.resolver

import com.coworkerinears.core.domain.brand.EarphoneBrandProfile
import com.coworkerinears.core.domain.brand.RecognitionRule
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot

interface EarphoneBrandResolver {
    fun resolve(snapshot: EarphoneDeviceSnapshot): EarphoneBrandResolution
}

class HeuristicEarphoneBrandResolver(
    private val profiles: List<EarphoneBrandProfile>
) : EarphoneBrandResolver {

    override fun resolve(snapshot: EarphoneDeviceSnapshot): EarphoneBrandResolution {
        val scoredProfiles = profiles.map { profile ->
            val matchedRules = profile.recognitionRules.filter { it.matches(snapshot) }
            val totalWeight = profile.recognitionRules.sumOf { it.weight }.takeIf { it > 0.0 } ?: 1.0
            val matchedWeight = matchedRules.sumOf { it.weight }
            val confidence = (matchedWeight / totalWeight).coerceIn(0.0, 1.0)
            Triple(profile, confidence, matchedRules)
        }

        val best = scoredProfiles.maxByOrNull { it.second }
        return if (best == null || best.second <= 0.0) {
            EarphoneBrandResolution(
                brand = null,
                confidence = 0.0,
                matchedRuleDescriptions = emptyList(),
                rationale = "No catalog rule matched the current device snapshot."
            )
        } else {
            EarphoneBrandResolution(
                brand = best.first.brand,
                confidence = best.second,
                matchedRuleDescriptions = best.third.map { it.description },
                rationale = "Matched ${best.first.brand.displayName} with heuristic snapshot rules."
            )
        }
    }
}

object DefaultEarphoneBrandResolver : EarphoneBrandResolver {
    private val delegate = HeuristicEarphoneBrandResolver(
        profiles = com.coworkerinears.core.data.catalog.DefaultEarphoneCatalog.profiles
    )

    override fun resolve(snapshot: EarphoneDeviceSnapshot): EarphoneBrandResolution {
        return delegate.resolve(snapshot)
    }
}
