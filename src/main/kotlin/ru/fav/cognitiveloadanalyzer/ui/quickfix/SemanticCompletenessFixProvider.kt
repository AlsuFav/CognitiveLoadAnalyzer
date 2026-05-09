package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

class SemanticCompletenessFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "CLC5" && criterion.riskLevel != RiskLevel.LOW

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Improve semantic annotations",
            description = """
                How to fix:

                Suggestions:
                • Add contentDescription
                • Define accessibility labels
                • Add explicit roles
                • Provide state descriptions
            """.trimIndent(),
            criterionId = "CLC5",
            filePath = filePath,
            canAutoFix = false
        )
}