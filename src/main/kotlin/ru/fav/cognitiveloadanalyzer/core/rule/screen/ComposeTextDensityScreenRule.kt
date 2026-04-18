package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.CriterionRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.screen.ComposeMetrics
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode

class ComposeTextDensityScreenRule : ScreenRule {

    override fun evaluate(screenStructure: ComposeUiNode): CriterionResult {
        val textCount = ComposeMetrics.textElementsCount(screenStructure)

        return CriterionResult(
            criterion = CriterionRegistry.CLC7,
            value = textCount.toDouble(),
            riskLevel = when {
                // Слишком много текста (информационная перегрузка)
                textCount > 20 -> RiskLevel.HIGH
                textCount > 12 -> RiskLevel.MEDIUM

                // Слишком мало текста (непонятный интерфейс)
                textCount == 0 -> RiskLevel.HIGH
                textCount < 3 -> RiskLevel.MEDIUM

                // Оптимальный диапазон: 3-12 текстовых элементов
                else -> RiskLevel.LOW
            },
            details = mapOf(
                "textElements" to textCount,
            )
        )
    }
}