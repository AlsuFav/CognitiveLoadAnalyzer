package ru.fav.cognitiveloadanalyzer.core.rule.navigation

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.navigation.NavigationMetrics
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph

class NavigationComplexityRule : NavigationRule {

    override fun evaluate(graph: NavigationGraph): CriterionResult {
        val metrics = NavigationMetrics.calculate(graph)
        
        return CriterionResult(
            criterion = CriterionRegistry.CLC11,
            value = metrics.complexityScore,
            riskLevel = when {
                metrics.complexityScore > 70 -> RiskLevel.HIGH
                metrics.complexityScore > 40 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
        )
    }
}