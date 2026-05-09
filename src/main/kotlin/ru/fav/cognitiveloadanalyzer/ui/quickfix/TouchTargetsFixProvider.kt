package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

class TouchTargetsFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "CLC4" && criterion.riskLevel != RiskLevel.LOW

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Reduce interactive element overload",
            description = """
                How to fix:

                Suggestions:
                • Merge related actions
                • Move secondary actions into menus
                • Prioritize primary CTA
            """.trimIndent(),
            criterionId = "CLC4",
            filePath = filePath,
            canAutoFix = false
        )
}