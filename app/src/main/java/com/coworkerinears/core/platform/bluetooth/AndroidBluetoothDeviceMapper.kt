package com.coworkerinears.core.platform.bluetooth

import android.bluetooth.BluetoothDevice
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot

object AndroidBluetoothDeviceMapper {
    fun toSnapshot(
        device: BluetoothDevice?,
        manufacturerHint: String? = null,
        bondedTags: Set<String> = emptySet(),
        isConnected: Boolean = false,
        supportsMediaKeyEvents: Boolean = true,
        supportsHandsFreeProfile: Boolean = true,
    ): EarphoneDeviceSnapshot {
        return EarphoneDeviceSnapshot(
            advertisedName = device?.name,
            manufacturerName = manufacturerHint,
            modelName = device?.name,
            bluetoothAddressHint = device?.address,
            bondedTags = bondedTags,
            supportsMediaKeyEvents = supportsMediaKeyEvents,
            supportsHandsFreeProfile = supportsHandsFreeProfile,
            isCurrentlyConnected = isConnected,
        )
    }
}
