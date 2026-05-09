package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

class DeepNestingFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "CLC9" && criterion.riskLevel != RiskLevel.LOW

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Reduce composable nesting depth",
            description = """
                How to fix:

                Suggestions:
                • Extract nested composables
                • Flatten layout hierarchy
                • Replace wrappers with Modifier chains
                • Split large composables into reusable blocks
            """.trimIndent(),
            criterionId = "CLC9",
            filePath = filePath,
            canAutoFix = false
        )
}