package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.*
import ru.fav.cognitiveloadanalyzer.core.model.*
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode

class ComposeHierarchyScreenRule : ScreenRule {

    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val depth = ComposeMetrics.depth(screenStructure)

        return CriterionResult(
            criterion = CriterionRegistry.CLC9,
            value = normalizeDepth(depth),
            rawValue = depth.toDouble(),
            riskLevel = when {
                depth > 12 -> RiskLevel.HIGH
                depth > 8 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "compose hierarchy depth" to depth,
            )
        )
    }

    private fun normalizeDepth(value: Int): Double =
        minOf((value / 10.0) * 100, 100.0)
}