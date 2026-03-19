package com.coworkerinears.core.data.memory

import com.coworkerinears.core.domain.memory.MemorySamples
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryMarkdownSerializerTest {
    @Test
    fun serialize_includesFrontmatterAndSections() {
        val markdown = MemoryMarkdownSerializer.serialize(MemorySamples.kickoffEntry())

        assertTrue(markdown.contains("id: mem_20260318_001"))
        assertTrue(markdown.contains("## Summary"))
        assertTrue(markdown.contains("## Raw Transcript"))
        assertTrue(markdown.contains("## Action Items"))
    }
}
