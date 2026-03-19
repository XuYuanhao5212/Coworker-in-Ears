package com.coworkerinears.core.data.memory

import com.coworkerinears.core.domain.memory.MemoryActionItem
import com.coworkerinears.core.domain.memory.MemoryEntry
import java.time.format.DateTimeFormatter

object MemoryMarkdownSerializer {
    private val fileDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    fun serialize(entry: MemoryEntry): String {
        return buildString {
            appendLine("---")
            appendLine("id: ${entry.id}")
            appendLine("created_at: ${entry.createdAt}")
            appendLine("source: ${entry.source}")
            appendLine("mode: ${entry.mode.name.lowercase()}")
            appendLine("title: ${yamlString(entry.title)}")
            appendLine("tags: ${yamlList(entry.tags)}")
            appendLine("participants: ${yamlList(entry.participants)}")
            appendLine("status: ${entry.status.name.lowercase()}")
            entry.audioReference?.let { appendLine("audio_reference: ${yamlString(it)}") }
            appendLine("---")
            appendLine()
            appendLine("# ${entry.title}")
            appendLine()
            appendLine("## Summary")
            appendLine()
            appendLine(entry.summary)
            appendLine()
            appendLine("## Raw Transcript")
            appendLine()
            appendLine(entry.rawTranscript)
            appendLine()
            appendLine("## Key Entities")
            appendLine()
            if (entry.keyEntities.isEmpty()) {
                appendLine("- None")
            } else {
                entry.keyEntities.forEach { appendLine("- $it") }
            }
            appendLine()
            appendLine("## Action Items")
            appendLine()
            if (entry.actionItems.isEmpty()) {
                appendLine("- [ ] None")
            } else {
                entry.actionItems.forEach { appendActionItem(it) }
            }
            appendLine()
            appendLine("## Notes")
            appendLine()
            appendLine(entry.notes.ifBlank { "None" })
        }.trimEnd()
    }

    fun suggestedRelativePath(entry: MemoryEntry): String {
        val datedFolder = entry.createdAt.format(fileDateFormatter)
        return "memory/$datedFolder/${entry.id}.md"
    }

    private fun StringBuilder.appendActionItem(item: MemoryActionItem) {
        val marker = if (item.isDone) "x" else " "
        appendLine("- [$marker] ${item.text}")
    }

    private fun yamlString(value: String): String {
        return buildString {
            append('"')
            value.forEach { character ->
                if (character == '"') {
                    append("\\\"")
                } else {
                    append(character)
                }
            }
            append('"')
        }
    }

    private fun yamlList(values: List<String>): String {
        if (values.isEmpty()) return "[]"
        return values.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ", ",
        ) { yamlString(it) }
    }
}
