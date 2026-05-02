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
            value = normalizeNavigation(metrics.complexityScore),
            rawValue = metrics.complexityScore,
            riskLevel = when {
                metrics.complexityScore > 70 -> RiskLevel.HIGH
                metrics.complexityScore > 40 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "transition count" to metrics.transitionCount,
                "cyclic transitions count" to metrics.cyclicTransitionsCount,
                "max outgoing transitions" to metrics.maxOutgoingTransitions,
                "avg outgoing transitions" to metrics.avgOutgoingTransitions,
                "max navigation depth" to metrics.maxNavigationDepth,
            )
        )
    }

    private fun normalizeNavigation(value: Double): Double =
        minOf((value / 70.0) * 100, 100.0)
}