package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel

class ComposeAnimationLoadScreenRule : ScreenRule {
    
    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val animations = ComposeMetrics.getAnimation(screenStructure)
        val animationCount = animations.size
        
        return CriterionResult(
            criterion = CriterionRegistry.CLC6,
            value = normalizeAnimation(animationCount),
            rawValue = animationCount.toDouble(),
            riskLevel = when {
                animationCount > 4 -> RiskLevel.HIGH
                animationCount > 2 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "animation elements count" to animationCount,
                "animation elements" to animations,
            )
        )
    }

    private fun normalizeAnimation(value: Int): Double =
        minOf((value / 4.0) * 100, 100.0)
}