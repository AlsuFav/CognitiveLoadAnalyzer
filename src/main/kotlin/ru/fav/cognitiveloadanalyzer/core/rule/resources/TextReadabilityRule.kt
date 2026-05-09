package ru.fav.cognitiveloadanalyzer.core.rule.resources

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.resources.ResourcesMetrics
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.resource.ResourceString

class TextReadabilityRule : ResourcesRule {

    override fun evaluate(strings: List<ResourceString>): CriterionResult {
        val metrics = ResourcesMetrics.calculateTextReadability(strings)

        return CriterionResult(
            criterion = CriterionRegistry.CLC2,
            value = metrics.complexityScore,
            rawValue = metrics.complexityScore,
            riskLevel = when {
                metrics.complexityScore > 70 -> RiskLevel.HIGH
                metrics.complexityScore > 40 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "average length" to metrics.avgLength,
                "long strings count" to metrics.longStrings.size,
                "long string ids" to metrics.longStrings.map { it.id }
            )
        )
    }
}