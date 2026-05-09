package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

class TooManyElementsFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "CLC1" && criterion.riskLevel != RiskLevel.LOW

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Split screen into sections",
            description = """
                How to fix:
                
                Suggestions:
                • Group related elements into LazyColumn sections
                • Move secondary actions to a bottom sheet or overflow menu
                • Consider splitting into multiple screens
            """.trimIndent(),
            criterionId = "CLC1",
            filePath = filePath,
            canAutoFix = false
        )
}