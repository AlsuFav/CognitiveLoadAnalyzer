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
            value = depth.toDouble(),
            riskLevel = when {
                depth > 10 -> RiskLevel.HIGH
                depth > 6 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }
        )
    }
}