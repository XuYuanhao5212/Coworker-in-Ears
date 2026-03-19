package com.coworkerinears.app.media

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import com.coworkerinears.core.data.memory.MarkdownMemoryRepository
import com.coworkerinears.core.data.resolver.DefaultEarphoneBrandResolver
import com.coworkerinears.core.data.transcription.HeuristicTranscriptStructurer
import com.coworkerinears.core.data.trigger.InMemorySessionScopedHotwordDetector
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import com.coworkerinears.core.domain.trigger.TriggerRequest
import com.coworkerinears.feature.capture.MemoryCaptureCoordinator
import com.coworkerinears.feature.capture.TriggeredMemoryCaptureCoordinator
import com.coworkerinears.feature.capture.TriggeredMemoryCaptureResult
import com.coworkerinears.feature.trigger.MediaButtonIntentParser
import com.coworkerinears.feature.trigger.TriggerCoordinator
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class LiveMediaButtonCapture(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val triggeredCoordinator = TriggeredMemoryCaptureCoordinator(
        triggerCoordinator = TriggerCoordinator(
            brandResolver = DefaultEarphoneBrandResolver,
            hotwordDetector = InMemorySessionScopedHotwordDetector(),
        ),
        memoryCaptureCoordinator = MemoryCaptureCoordinator(
            transcriptStructurer = HeuristicTranscriptStructurer(),
            memoryRepository = MarkdownMemoryRepository(
                rootDirectory = File(appContext.filesDir, "validation-lab"),
            ),
        ),
    )

    private var mediaSession: MediaSession? = null

    fun activate() {
        if (mediaSession != null) return

        val session = MediaSession(appContext, SESSION_TAG).apply {
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setPlaybackState(buildPlaybackState())
            setCallback(object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                    val keyEvent = mediaButtonIntent.extractKeyEvent()
                    if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_UP) {
                        return super.onMediaButtonEvent(mediaButtonIntent)
                    }

                    val snapshot = resolveActiveBluetoothSnapshot()
                    val request = MediaButtonIntentParser.parse(
                        intent = mediaButtonIntent,
                        deviceSnapshot = snapshot,
                    )
                    val event = when {
                        snapshot == null -> LiveMediaKeyEvent(
                            timeLabel = timeLabel(),
                            keyLabel = keyLabel(keyEvent.keyCode),
                            outcome = LiveMediaKeyOutcome.NO_ACTIVE_ROUTE,
                        )

                        request == null -> LiveMediaKeyEvent(
                            timeLabel = timeLabel(),
                            keyLabel = keyLabel(keyEvent.keyCode),
                            deviceName = snapshot.modelName ?: snapshot.advertisedName,
                            resolvedBrandLabel = DefaultEarphoneBrandResolver.resolve(snapshot).brand?.displayName,
                            outcome = LiveMediaKeyOutcome.UNSUPPORTED_KEY,
                        )

                        else -> recordAcceptedRequest(
                            keyEvent = keyEvent,
                            snapshot = snapshot,
                            request = request,
                        )
                    }

                    Log.d(LOG_TAG, "Media key ${event.keyLabel} => ${event.outcome} device=${event.deviceName}")
                    LiveMediaKeyDebugStore.record(event)
                    return event.outcome == LiveMediaKeyOutcome.CAPTURED
                }
            })
            isActive = true
        }

        mediaSession = session
        LiveMediaKeyDebugStore.markSessionArmed(true)
    }

    fun release() {
        mediaSession?.release()
        mediaSession = null
        LiveMediaKeyDebugStore.markSessionArmed(false)
    }

    private fun recordAcceptedRequest(
        keyEvent: KeyEvent,
        snapshot: EarphoneDeviceSnapshot,
        request: TriggerRequest,
    ): LiveMediaKeyEvent {
        val result = triggeredCoordinator.process(
            request = request,
            rawTranscript = buildTranscript(snapshot, keyEvent),
            title = buildTitle(snapshot, keyEvent),
            tags = listOf("live", "media_key", "hardware"),
            now = OffsetDateTime.now(),
        )

        return when (result) {
            is TriggeredMemoryCaptureResult.Success -> LiveMediaKeyEvent(
                timeLabel = timeLabel(),
                keyLabel = keyLabel(keyEvent.keyCode),
                deviceName = snapshot.modelName ?: snapshot.advertisedName,
                resolvedBrandLabel = result.validation.activationPlan.brandResolution.brand?.displayName,
                outcome = LiveMediaKeyOutcome.CAPTURED,
                markdownPath = result.capture.destination.absolutePath,
            )

            is TriggeredMemoryCaptureResult.Rejected -> LiveMediaKeyEvent(
                timeLabel = timeLabel(),
                keyLabel = keyLabel(keyEvent.keyCode),
                deviceName = snapshot.modelName ?: snapshot.advertisedName,
                resolvedBrandLabel = result.validation.activationPlan.brandResolution.brand?.displayName,
                outcome = LiveMediaKeyOutcome.REJECTED,
                rejectionReason = result.validation.rejectionReason,
            )

            TriggeredMemoryCaptureResult.EmptyTranscript -> LiveMediaKeyEvent(
                timeLabel = timeLabel(),
                keyLabel = keyLabel(keyEvent.keyCode),
                deviceName = snapshot.modelName ?: snapshot.advertisedName,
                resolvedBrandLabel = DefaultEarphoneBrandResolver.resolve(snapshot).brand?.displayName,
                outcome = LiveMediaKeyOutcome.REJECTED,
                rejectionReason = "Transcript cannot be empty.",
            )
        }
    }

    private fun resolveActiveBluetoothSnapshot(): EarphoneDeviceSnapshot? {
        val audioManager = appContext.getSystemService(AudioManager::class.java) ?: return null
        val bluetoothTypes = buildSet {
            add(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
            add(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(AudioDeviceInfo.TYPE_BLE_HEADSET)
                add(AudioDeviceInfo.TYPE_BLE_SPEAKER)
            }
        }

        val device = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { candidate ->
                candidate.type in bluetoothTypes && candidate.productName?.toString().orEmpty().isNotBlank()
            }
            ?: return null

        val name = device.productName?.toString().orEmpty().ifBlank { null }
        return EarphoneDeviceSnapshot(
            advertisedName = name,
            manufacturerName = null,
            modelName = name,
            bluetoothAddressHint = device.address.takeIf { it.isNotBlank() },
            bondedTags = tokenize(name),
            supportsMediaKeyEvents = true,
            supportsHandsFreeProfile = device.type != AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            isCurrentlyConnected = true,
        )
    }

    private fun buildTranscript(snapshot: EarphoneDeviceSnapshot, keyEvent: KeyEvent): String {
        val device = snapshot.modelName ?: snapshot.advertisedName ?: "connected earphone"
        return "Live media-key trigger from $device. Android delivered ${keyLabel(keyEvent.keyCode)} and the app turned it into a Markdown memory entry during hardware validation."
    }

    private fun buildTitle(snapshot: EarphoneDeviceSnapshot, keyEvent: KeyEvent): String {
        val device = snapshot.modelName ?: snapshot.advertisedName ?: "Earphone"
        return "$device ${keyLabel(keyEvent.keyCode)}"
    }

    private fun tokenize(name: String?): Set<String> {
        return name.orEmpty()
            .split(Regex("[^A-Za-z0-9]+"))
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun buildPlaybackState(): PlaybackState {
        val actions = PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_FAST_FORWARD or
            PlaybackState.ACTION_REWIND
        return PlaybackState.Builder()
            .setActions(actions)
            .setState(PlaybackState.STATE_PAUSED, 0L, 1.0f)
            .build()
    }

    private fun Intent.extractKeyEvent(): KeyEvent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        }
    }

    private fun keyLabel(keyCode: Int): String {
        return KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
    }

    private fun timeLabel(): String {
        return OffsetDateTime.now().format(TIME_FORMATTER)
    }

    private companion object {
        const val SESSION_TAG = "CoworkerLiveMediaButton"
        const val LOG_TAG = "CoworkerMediaKey"
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}