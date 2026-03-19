package com.coworkerinears.feature.home

data class HomeUiState(
    val heroEyebrow: String,
    val heroTitle: String,
    val heroSubtitle: String,
    val releaseStatus: String,
    val releaseSummary: String,
    val readinessScore: Float,
    val readinessStats: List<ConsoleStatUiModel>,
    val supportedBrands: List<BrandSupportUiModel>,
    val triggerChannels: List<TriggerChannelUiModel>,
    val memoryPathHint: String,
    val memoryHeadline: String,
    val memoryPreview: String,
    val guardrails: List<String>,
    val nextActions: List<ActionPanelUiModel>,
    val debugChecklist: List<String>,
)

data class ConsoleStatUiModel(
    val label: String,
    val value: String,
    val supportingText: String,
)

data class BrandSupportUiModel(
    val name: String,
    val supportState: String,
    val notes: String,
    val triggerModes: List<String>,
)

data class TriggerChannelUiModel(
    val title: String,
    val availability: String,
    val summary: String,
)

data class ActionPanelUiModel(
    val title: String,
    val detail: String,
    val actionHint: String,
)
