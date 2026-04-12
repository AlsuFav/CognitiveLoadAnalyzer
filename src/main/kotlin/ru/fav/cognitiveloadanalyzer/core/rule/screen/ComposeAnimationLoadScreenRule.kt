package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel

class ComposeAnimationLoadScreenRule : ScreenRule {
    
    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val animationCount = ComposeMetrics.animationCount(screenStructure)
        
        return CriterionResult(
            criterion = CriterionRegistry.CLC6,
            value = animationCount.toDouble(),
            riskLevel = when {
                animationCount > 4 -> RiskLevel.HIGH
                animationCount > 2 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }
        )
    }
}