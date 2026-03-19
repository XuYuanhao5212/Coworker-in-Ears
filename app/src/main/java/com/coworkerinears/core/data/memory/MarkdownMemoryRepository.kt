package com.coworkerinears.core.data.memory

import com.coworkerinears.core.domain.memory.MemoryEntry
import java.io.File

class MarkdownMemoryRepository(
    private val rootDirectory: File,
) {
    fun save(entry: MemoryEntry): File {
        val destination = fileFor(entry)
        destination.parentFile?.mkdirs()
        destination.writeText(
            text = MemoryMarkdownSerializer.serialize(entry),
            charset = Charsets.UTF_8,
        )
        return destination
    }

    fun read(entry: MemoryEntry): String {
        return fileFor(entry).readText(Charsets.UTF_8)
    }

    fun exists(entry: MemoryEntry): Boolean {
        return fileFor(entry).exists()
    }

    fun fileFor(entry: MemoryEntry): File {
        return File(rootDirectory, MemoryMarkdownSerializer.suggestedRelativePath(entry))
    }

    fun listMarkdownFiles(limit: Int = Int.MAX_VALUE): List<File> {
        if (!rootDirectory.exists()) return emptyList()

        return rootDirectory
            .walkTopDown()
            .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
            .sortedByDescending { it.lastModified() }
            .take(limit)
            .toList()
    }

    fun readFile(file: File): String {
        return file.readText(Charsets.UTF_8)
    }
}
