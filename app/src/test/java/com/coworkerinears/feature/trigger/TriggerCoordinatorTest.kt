package com.coworkerinears.feature.trigger

import com.coworkerinears.core.data.resolver.DefaultEarphoneBrandResolver
import com.coworkerinears.core.data.trigger.InMemorySessionScopedHotwordDetector
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode
import com.coworkerinears.core.domain.trigger.SessionHotwordSpec
import com.coworkerinears.core.domain.trigger.TriggerRequest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerCoordinatorTest {
    private val coordinator = TriggerCoordinator(
        brandResolver = DefaultEarphoneBrandResolver,
        hotwordDetector = InMemorySessionScopedHotwordDetector(),
    )

    @Test
    fun validate_acceptsSupportedBrandMediaKeyRequest() {
        val result = coordinator.validate(
            TriggerRequest(
                channel = EarphoneTriggerMode.MEDIA_KEY,
                deviceSnapshot = EarphoneDeviceSnapshot(
                    advertisedName = "HUAWEI FreeBuds Pro",
                    manufacturerName = "HUAWEI",
                    supportsMediaKeyEvents = true,
                    isCurrentlyConnected = true,
                ),
            ),
        )

        assertTrue(result.accepted)
        assertTrue(result.activationPlan.brandResolution.brand != null)
    }

    @Test
    fun validate_rejectsUnknownBrandSnapshot() {
        val result = coordinator.validate(
            TriggerRequest(
                channel = EarphoneTriggerMode.MEDIA_KEY,
                deviceSnapshot = EarphoneDeviceSnapshot(
                    advertisedName = "Mystery Buds X",
                    manufacturerName = "Unknown",
                    supportsMediaKeyEvents = true,
                    isCurrentlyConnected = true,
                ),
            ),
        )

        assertFalse(result.accepted)
        assertTrue(result.rejectionReason?.contains("supported brands") == true)
    }

    @Test
    fun validate_rejectsSessionHotwordWithoutSpec() {
        val result = coordinator.validate(
            TriggerRequest(
                channel = EarphoneTriggerMode.SESSION_HOTWORD,
            ),
        )

        assertFalse(result.accepted)
        assertTrue(result.rejectionReason?.contains("phrase specification") == true)
    }

    @Test
    fun validate_acceptsAppOwnedSessionHotwordWhenSpecIsPresent() {
        val result = coordinator.validate(
            TriggerRequest(
                channel = EarphoneTriggerMode.SESSION_HOTWORD,
                sessionHotwordSpec = SessionHotwordSpec("Hey Coworker"),
            ),
        )

        assertTrue(result.accepted)
    }
}
