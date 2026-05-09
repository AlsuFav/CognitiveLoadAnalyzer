package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

class ContextRelevanceFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "CLC7" && criterion.riskLevel != RiskLevel.LOW

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Show needed contextual information",
            description = """
                How to fix:

                Suggestions:
                • Show necessary information
                • Hide secondary details
                • Show information only when relevant
                • Prioritize task-critical content
            """.trimIndent(),
            criterionId = "CLC7",
            filePath = filePath,
            canAutoFix = false
        )
}