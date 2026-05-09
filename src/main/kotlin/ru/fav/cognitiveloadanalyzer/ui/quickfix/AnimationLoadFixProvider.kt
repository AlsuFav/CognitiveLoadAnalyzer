package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

class AnimationLoadFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "CLC6" && criterion.riskLevel != RiskLevel.LOW

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Reduce animation complexity",
            description = """
                How to fix:

                Suggestions:
                • Remove non-essential transitions
                • Avoid simultaneous animations
                • Prefer subtle motion cues
            """.trimIndent(),
            criterionId = "CLC6",
            filePath = filePath,
            canAutoFix = false
        )
}