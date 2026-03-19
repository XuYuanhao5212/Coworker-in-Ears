package com.coworkerinears.core.data.transcription

import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicTranscriptStructurerTest {
    private val structurer = HeuristicTranscriptStructurer()

    @Test
    fun structure_extractsKnownEntitiesAndActionHints() {
        val result = structurer.structure(
            """
                User: Please follow up with Huawei and OPPO validation this week.
                Assistant: Next step is to connect the Markdown export flow.
            """.trimIndent(),
        )

        assertTrue(result.summary.contains("Please follow up"))
        assertTrue(result.keyEntities.contains("Huawei"))
        assertTrue(result.keyEntities.contains("OPPO"))
        assertTrue(result.actionItems.any { it.contains("follow up", ignoreCase = true) })
        assertTrue(result.actionItems.any { it.contains("Next step", ignoreCase = true) })
    }
}
