package com.coworkerinears.core.data.catalog

import com.coworkerinears.core.domain.brand.EarphoneBrand
import com.coworkerinears.core.domain.brand.EarphoneBrandProfile
import com.coworkerinears.core.domain.brand.RecognitionRule
import com.coworkerinears.core.domain.capability.EarphoneCapability
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode

object DefaultEarphoneCatalog {
    val profiles: List<EarphoneBrandProfile> = listOf(
        EarphoneBrandProfile(
            brand = EarphoneBrand.HUAWEI,
            recognitionRules = listOf(
                RecognitionRule.AnyOf(
                    rules = listOf(
                        RecognitionRule.NameContains(setOf("huawei", "freebuds", "freelace", "freeclip", "matebuds")),
                        RecognitionRule.ManufacturerContains(setOf("huawei"))
                    )
                )
            ),
            capability = EarphoneCapability(
                supportedTriggerModes = setOf(
                    EarphoneTriggerMode.MEDIA_KEY,
                    EarphoneTriggerMode.PUSH_TO_TALK,
                    EarphoneTriggerMode.SESSION_HOTWORD
                ),
                notes = "Huawei-branded devices are matched by brand and product-name hints first."
            ),
            notes = "First-pass compatibility rule set for Huawei earphones."
        ),
        EarphoneBrandProfile(
            brand = EarphoneBrand.LENOVO,
            recognitionRules = listOf(
                RecognitionRule.AnyOf(
                    rules = listOf(
                        RecognitionRule.NameContains(setOf("lenovo", "thinkplus")),
                        RecognitionRule.ManufacturerContains(setOf("lenovo"))
                    )
                )
            ),
            capability = EarphoneCapability(
                supportedTriggerModes = setOf(
                    EarphoneTriggerMode.MEDIA_KEY,
                    EarphoneTriggerMode.PUSH_TO_TALK,
                    EarphoneTriggerMode.SESSION_HOTWORD
                ),
                notes = "Lenovo support starts with conservative token matching and can be expanded by device lab validation."
            ),
            notes = "First-pass compatibility rule set for Lenovo earphones."
        ),
        EarphoneBrandProfile(
            brand = EarphoneBrand.OPPO,
            recognitionRules = listOf(
                RecognitionRule.AnyOf(
                    rules = listOf(
                        RecognitionRule.NameContains(setOf("oppo", "enco")),
                        RecognitionRule.ManufacturerContains(setOf("oppo"))
                    )
                )
            ),
            capability = EarphoneCapability(
                supportedTriggerModes = setOf(
                    EarphoneTriggerMode.MEDIA_KEY,
                    EarphoneTriggerMode.PUSH_TO_TALK,
                    EarphoneTriggerMode.SESSION_HOTWORD
                ),
                notes = "OPPO support is intentionally name-driven because accessory naming varies by market."
            ),
            notes = "First-pass compatibility rule set for OPPO earphones."
        ),
        EarphoneBrandProfile(
            brand = EarphoneBrand.HONOR,
            recognitionRules = listOf(
                RecognitionRule.AnyOf(
                    rules = listOf(
                        RecognitionRule.NameContains(setOf("honor", "earbuds", "choice")),
                        RecognitionRule.ManufacturerContains(setOf("honor"))
                    )
                )
            ),
            capability = EarphoneCapability(
                supportedTriggerModes = setOf(
                    EarphoneTriggerMode.MEDIA_KEY,
                    EarphoneTriggerMode.PUSH_TO_TALK,
                    EarphoneTriggerMode.SESSION_HOTWORD
                ),
                notes = "Honor support uses a broad but conservative set of name hints."
            ),
            notes = "First-pass compatibility rule set for Honor earphones."
        )
    )
}
