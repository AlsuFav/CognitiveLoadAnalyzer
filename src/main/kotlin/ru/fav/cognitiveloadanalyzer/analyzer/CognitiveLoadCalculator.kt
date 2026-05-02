package ru.fav.cognitiveloadanalyzer.analyzer

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

object CognitiveLoadCalculator {

    fun calculate(results: List<CriterionResult>): Double {
        if (results.isEmpty()) return 0.0

        val weightedSum = results.sumOf { result ->
            val weight = result.criterion.weight
            (result.value / 100.0) * weight
        }

        val actualWeight = results.sumOf { it.criterion.weight }

        if (actualWeight == 0.0) return 0.0

        return (weightedSum / actualWeight) * 100.0
    }
}