package ru.fav.cognitiveloadanalyzer.analyzer.resources

import ru.fav.cognitiveloadanalyzer.core.model.resource.ResourceString
import ru.fav.cognitiveloadanalyzer.core.model.resource.TextReadabilityResult

object ResourcesMetrics {

    fun calculateTextReadability(strings: List<ResourceString>): TextReadabilityResult {
        if (strings.isEmpty()) {
            return TextReadabilityResult(0.0, emptyList(), 0.0)
        }

        val avgLength = strings.map { it.value.length }.average()

        val longStrings = strings.filter {
            it.value.length > 120
        }

        val avgWords = strings
            .map { countWords(it.value) }
            .average()

        val complexityScore = calculateTextReadabilityScore(
            avgLength,
            avgWords,
            longStrings.size,
            strings.size
        )

        return TextReadabilityResult(
            avgLength = avgLength,
            longStrings = longStrings,
            complexityScore = complexityScore
        )
    }

    private fun countWords(text: String): Int =
        text.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .size

    private fun calculateTextReadabilityScore(
        avgLength: Double,
        avgWords: Double,
        longCount: Int,
        total: Int
    ): Double {
        val lengthScore = minOf(avgLength / 120.0, 1.0)
        val wordScore = minOf(avgWords / 25.0, 1.0)
        val longRatio = if (total > 0) longCount.toDouble() / total else 0.0

        return (
            lengthScore * 0.45 +
            wordScore * 0.35 +
            longRatio * 0.20
        ) * 100
    }
}