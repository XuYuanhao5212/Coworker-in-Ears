package com.coworkerinears.app.validation

import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot

enum class ValidationDevicePreset(
    val displayName: String,
    val manufacturerName: String,
    val advertisedName: String,
    val connectedByDefault: Boolean = true,
) {
    HUAWEI(
        displayName = "Huawei FreeBuds",
        manufacturerName = "HUAWEI",
        advertisedName = "HUAWEI FreeBuds Pro 3",
    ),
    LENOVO(
        displayName = "Lenovo ThinkPlus",
        manufacturerName = "Lenovo",
        advertisedName = "Lenovo ThinkPlus Live Pods",
    ),
    OPPO(
        displayName = "OPPO Enco",
        manufacturerName = "OPPO",
        advertisedName = "OPPO Enco Air 4 Pro",
    ),
    HONOR(
        displayName = "Honor Earbuds",
        manufacturerName = "HONOR",
        advertisedName = "HONOR Earbuds X7",
    ),
    UNKNOWN(
        displayName = "Unknown earphone",
        manufacturerName = "Unknown",
        advertisedName = "Mystery Pods",
    ),
}

fun ValidationDevicePreset.toSnapshot(): EarphoneDeviceSnapshot {
    return EarphoneDeviceSnapshot(
        advertisedName = advertisedName,
        manufacturerName = manufacturerName,
        modelName = advertisedName,
        bondedTags = setOf(advertisedName.substringBefore(" ")),
        supportsMediaKeyEvents = true,
        supportsHandsFreeProfile = true,
        isCurrentlyConnected = connectedByDefault,
    )
}
