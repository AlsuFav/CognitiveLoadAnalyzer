package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode

class ComposeReusabilityRule : ScreensRule {

    override fun evaluate(screens: List<ComposeUiNode>): CriterionResult {
        val metrics = ComposeMetrics.reusabilityMetrics(screens)

        return CriterionResult(
            criterion = CriterionRegistry.CLC10,
            value = metrics.reusabilityRatio,
            riskLevel = when {
                metrics.reusabilityRatio < 20 -> RiskLevel.HIGH
                metrics.reusabilityRatio < 50 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "totalComponents" to metrics.totalComponents,
                "uniqueComponents" to metrics.uniqueComponents,
                "reusabilityRatio" to "%.1f%%".format(metrics.reusabilityRatio)
            )
        )
    }
}