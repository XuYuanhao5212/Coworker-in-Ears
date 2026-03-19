package com.coworkerinears.core.domain.device

/**
 * Snapshot of the earphone state observed by the app.
 *
 * The model stays app-owned so we do not over-couple the compatibility layer
 * to any specific Android Bluetooth API surface yet.
 */
data class EarphoneDeviceSnapshot(
    val advertisedName: String? = null,
    val manufacturerName: String? = null,
    val modelName: String? = null,
    val bluetoothAddressHint: String? = null,
    val bondedTags: Set<String> = emptySet(),
    val supportsMediaKeyEvents: Boolean = false,
    val supportsHandsFreeProfile: Boolean = false,
    val isCurrentlyConnected: Boolean = false
)
