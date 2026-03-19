package com.coworkerinears.core.data.transcription

import com.coworkerinears.core.domain.transcription.StructuredTranscript
import com.coworkerinears.core.domain.transcription.TranscriptStructurer
import java.util.Locale

/**
 * Lightweight structuring heuristic for the MVP shell.
 *
 * This is not an ASR or LLM replacement. It only gives the project a local,
 * testable way to turn a transcript into summary/entities/actions until the
 * real provider layer is wired in.
 */
class HeuristicTranscriptStructurer : TranscriptStructurer {
    override fun structure(rawTranscript: String): StructuredTranscript {
        val cleanedLines = rawTranscript
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val summary = cleanedLines.firstOrNull()
            ?.let(::stripSpeakerPrefix)
            ?.take(160)
            ?: "No transcript content yet."

        val keyEntities = extractEntities(cleanedLines)
        val actionItems = extractActionItems(cleanedLines)

        return StructuredTranscript(
            summary = summary,
            keyEntities = keyEntities,
            actionItems = actionItems,
        )
    }

    private fun extractEntities(lines: List<String>): List<String> {
        val matches = mutableSetOf<String>()
        val regex = Regex("\\b[A-Z][A-Za-z0-9.+-]{2,}\\b")
        lines.forEach { line ->
            regex.findAll(line).forEach { matches += it.value }
        }

        knownEntities.forEach { entity ->
            if (lines.any { it.contains(entity, ignoreCase = true) }) {
                matches += entity
            }
        }

        return matches
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(8)
    }

    private fun extractActionItems(lines: List<String>): List<String> {
        val actionHints = listOf(
            "need to",
            "todo",
            "follow up",
            "please",
            "next step",
            "需要",
            "待办",
            "跟进",
            "请",
            "下一步",
        )

        val results = lines.filter { line ->
            val normalized = line.lowercase(Locale.ROOT)
            actionHints.any { normalized.contains(it) }
        }

        return results
            .map(::stripSpeakerPrefix)
            .map { it.removePrefix("-").trim() }
            .distinct()
            .take(5)
    }

    private fun stripSpeakerPrefix(line: String): String {
        return line.replace(Regex("^[\\p{L}0-9_\\- ]{1,20}:\\s*"), "").trim()
    }

    companion object {
        private val knownEntities = listOf(
            "Android",
            "Huawei",
            "Lenovo",
            "OPPO",
            "Honor",
            "Markdown",
            "Slack",
            "Calendar",
        )
    }
}
