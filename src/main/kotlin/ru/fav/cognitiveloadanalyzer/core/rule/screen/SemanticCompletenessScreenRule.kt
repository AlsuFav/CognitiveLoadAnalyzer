package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode

class SemanticCompletenessScreenRule : ScreenRule {

    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val metrics = ComposeMetrics.semanticCompleteness(screenStructure)

        return CriterionResult(
            criterion = CriterionRegistry.CLC5,
            value = normalizeSemanticMissing(metrics.missingSemanticRatio),
            rawValue = metrics.missingSemanticRatio,
            riskLevel = when {
                metrics.missingSemanticRatio > 40 -> RiskLevel.HIGH
                metrics.missingSemanticRatio > 20 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "total elements with semantics need" to metrics.totalElements,
                "with semantics" to metrics.elementsWithSemantics,
                "without semantics" to metrics.elementsWithoutSemantics,
                "elements with semantics need" to metrics.nodes,
            )
        )
    }

    private fun normalizeSemanticMissing(value: Double): Double =
        minOf((value / 40.0) * 100, 100.0)
}