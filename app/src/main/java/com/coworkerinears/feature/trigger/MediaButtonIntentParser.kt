package com.coworkerinears.feature.trigger

import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode
import com.coworkerinears.core.domain.trigger.TriggerRequest

object MediaButtonIntentParser {
    fun parse(
        intent: Intent,
        deviceSnapshot: EarphoneDeviceSnapshot? = null,
    ): TriggerRequest? {
        if (intent.action != Intent.ACTION_MEDIA_BUTTON) return null

        val keyEvent = intent.keyEvent() ?: return null
        if (keyEvent.action != KeyEvent.ACTION_UP) return null

        val supported = setOf(
            KeyEvent.KEYCODE_HEADSETHOOK,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_STOP,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_REWIND,
        )
        if (keyEvent.keyCode !in supported) return null

        return TriggerRequest(
            channel = EarphoneTriggerMode.MEDIA_KEY,
            deviceSnapshot = deviceSnapshot,
        )
    }

    @Suppress("DEPRECATION")
    private fun Intent.keyEvent(): KeyEvent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        }
    }
}
