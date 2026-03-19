package com.coworkerinears.core.domain.transcription

data class StructuredTranscript(
    val summary: String,
    val keyEntities: List<String>,
    val actionItems: List<String>,
)

interface TranscriptStructurer {
    fun structure(rawTranscript: String): StructuredTranscript
}
