package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.compose.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.analyzer.compose.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel

class ComposeClickableElementsScreenRule : ScreenRule {
    
    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val clickableCount = ComposeMetrics.clickableElementsCount(screenStructure)
        
        return CriterionResult(
            criterion = CriterionRegistry.CLC4,
            value = clickableCount.toDouble(),
            riskLevel = when {
                clickableCount > 12 -> RiskLevel.HIGH
                clickableCount > 6 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }
        )
    }
}