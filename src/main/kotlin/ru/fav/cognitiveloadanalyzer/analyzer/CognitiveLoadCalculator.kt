package ru.fav.cognitiveloadanalyzer.analyzer

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

object CognitiveLoadCalculator {

    fun calculate(results: List<CriterionResult>): Double {
        return results.sumOf { result ->
            val weight = CriterionRegistry.all
                .find { it == result.criterion }
                ?.weight ?: 0.0

            weight * result.value
        }
    }
}