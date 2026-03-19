package com.coworkerinears.app.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import com.coworkerinears.core.data.resolver.DefaultEarphoneBrandResolver
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import java.util.Locale

data class HardwareProbeDevice(
    val displayName: String,
    val addressHint: String,
    val resolvedBrandLabel: String,
    val isSupported: Boolean,
    val rationale: String,
)

data class HardwareProbeResult(
    val devices: List<HardwareProbeDevice>,
    val status: HardwareProbeStatus,
)

enum class HardwareProbeStatus {
    READY,
    BLUETOOTH_UNAVAILABLE,
    PERMISSION_REQUIRED,
    NO_BONDED_DEVICES,
}

object HardwareProbe {
    @SuppressLint("MissingPermission")
    fun load(context: Context, hasBluetoothPermission: Boolean): HardwareProbeResult {
        if (!hasBluetoothPermission) {
            return HardwareProbeResult(
                devices = emptyList(),
                status = HardwareProbeStatus.PERMISSION_REQUIRED,
            )
        }

        val adapter = bluetoothAdapter(context)
            ?: return HardwareProbeResult(emptyList(), HardwareProbeStatus.BLUETOOTH_UNAVAILABLE)

        val bondedDevices = try {
            adapter.bondedDevices.orEmpty()
        } catch (_: SecurityException) {
            return HardwareProbeResult(emptyList(), HardwareProbeStatus.PERMISSION_REQUIRED)
        }

        if (bondedDevices.isEmpty()) {
            return HardwareProbeResult(emptyList(), HardwareProbeStatus.NO_BONDED_DEVICES)
        }

        val devices = bondedDevices
            .sortedBy { it.name.orEmpty() }
            .map { device ->
                val snapshot = EarphoneDeviceSnapshot(
                    advertisedName = device.name,
                    manufacturerName = null,
                    modelName = device.name,
                    bluetoothAddressHint = device.address,
                    bondedTags = tokenize(device.name),
                    supportsMediaKeyEvents = true,
                    supportsHandsFreeProfile = true,
                    isCurrentlyConnected = false,
                )
                val resolution = DefaultEarphoneBrandResolver.resolve(snapshot)
                HardwareProbeDevice(
                    displayName = device.name ?: "Unknown Bluetooth device",
                    addressHint = maskAddress(device.address),
                    resolvedBrandLabel = resolution.brand?.displayName ?: "Unsupported / unknown",
                    isSupported = resolution.brand != null,
                    rationale = resolution.rationale,
                )
            }

        return HardwareProbeResult(
            devices = devices,
            status = HardwareProbeStatus.READY,
        )
    }

    private fun bluetoothAdapter(context: Context): BluetoothAdapter? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(BluetoothManager::class.java)?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    private fun tokenize(name: String?): Set<String> {
        return name.orEmpty()
            .split(Regex("[^A-Za-z0-9]+"))
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun maskAddress(address: String?): String {
        if (address.isNullOrBlank()) return "--"
        val parts = address.split(':')
        if (parts.size < 2) return address
        return "**:**:**:${parts.takeLast(3).joinToString(":")}" 
    }
}
