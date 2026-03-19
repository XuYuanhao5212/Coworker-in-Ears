package com.coworkerinears.app.validation

data class ValidationPermissionGuide(
    val recordAudioGranted: Boolean,
    val bluetoothConnectGranted: Boolean,
    val canRunEarphoneValidation: Boolean,
    val statusHeadline: String,
    val nextSteps: List<String>,
)
