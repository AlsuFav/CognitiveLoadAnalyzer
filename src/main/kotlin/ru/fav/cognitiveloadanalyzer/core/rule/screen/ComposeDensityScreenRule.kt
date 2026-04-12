package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.*

class ComposeDensityScreenRule : ScreenRule {

    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val density = ComposeMetrics.density(screenStructure)

        return CriterionResult(
            criterion = CriterionRegistry.CLC1,
            value = density.toDouble(),
            riskLevel = when {
                density > 20 -> RiskLevel.HIGH
                density > 12 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
        )
    }
}