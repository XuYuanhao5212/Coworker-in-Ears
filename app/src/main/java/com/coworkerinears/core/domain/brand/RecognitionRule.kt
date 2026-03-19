package com.coworkerinears.core.domain.brand

import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import java.util.Locale

sealed class RecognitionRule(
    val weight: Double,
    val description: String
) {
    abstract fun matches(snapshot: EarphoneDeviceSnapshot): Boolean

    protected fun normalized(value: String?): String {
        return value
            ?.lowercase(Locale.ROOT)
            ?.replace(NORMALIZE_PATTERN, "")
            .orEmpty()
    }

    protected fun containsAny(source: String?, candidates: Set<String>): Boolean {
        val normalizedSource = normalized(source)
        return candidates.any { token ->
            normalizedSource.contains(normalized(token))
        }
    }

    companion object {
        private val NORMALIZE_PATTERN = Regex("[\\p{Punct}\\s]+")
    }

    class NameContains(
        private val tokens: Set<String>,
        weight: Double = 0.65,
        description: String = "device name contains one of $tokens"
    ) : RecognitionRule(weight, description) {
        override fun matches(snapshot: EarphoneDeviceSnapshot): Boolean {
            return containsAny(snapshot.advertisedName, tokens) ||
                containsAny(snapshot.modelName, tokens) ||
                snapshot.bondedTags.any { tag -> containsAny(tag, tokens) }
        }
    }

    class ManufacturerContains(
        private val tokens: Set<String>,
        weight: Double = 0.85,
        description: String = "manufacturer contains one of $tokens"
    ) : RecognitionRule(weight, description) {
        override fun matches(snapshot: EarphoneDeviceSnapshot): Boolean {
            return containsAny(snapshot.manufacturerName, tokens)
        }
    }

    class AllOf(
        private val rules: List<RecognitionRule>,
        weight: Double = 1.0,
        description: String = "all nested rules must match"
    ) : RecognitionRule(weight, description) {
        override fun matches(snapshot: EarphoneDeviceSnapshot): Boolean {
            return rules.all { it.matches(snapshot) }
        }
    }

    class AnyOf(
        private val rules: List<RecognitionRule>,
        weight: Double = 1.0,
        description: String = "any nested rule may match"
    ) : RecognitionRule(weight, description) {
        override fun matches(snapshot: EarphoneDeviceSnapshot): Boolean {
            return rules.any { it.matches(snapshot) }
        }
    }
}
