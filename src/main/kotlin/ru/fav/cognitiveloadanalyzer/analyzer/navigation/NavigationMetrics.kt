package ru.fav.cognitiveloadanalyzer.analyzer.navigation

import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationComplexityResult
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph

/**
 * Вычисляет метрики навигационной сложности
 */
object NavigationMetrics {

    fun calculate(graph: NavigationGraph): NavigationComplexityResult {
        val routeCount = graph.routes.size
        val transitionCount = graph.transitions.size

        // Подсчитываем исходящие переходы для каждого экрана
        val outgoingTransitions = graph.transitions
            .groupBy { it.from }
            .mapValues { it.value.size }

        val maxOutgoing = outgoingTransitions.values.maxOrNull() ?: 0
        val avgOutgoing = if (outgoingTransitions.isNotEmpty()) {
            outgoingTransitions.values.average()
        } else {
            0.0
        }

        // Подсчитываем циклические переходы
        val cyclicTransitions = graph.findCycles()

        // Вычисляем максимальную глубину навигации
        val maxDepth = graph.calculateMaxDepth()

        // Complexity Score (0-100)
        val complexityScore = calculateComplexityScore(
            routeCount = routeCount,
            transitionCount = transitionCount,
            maxOutgoing = maxOutgoing,
            cyclicCount = cyclicTransitions.size,
            maxDepth = maxDepth,
            avgOutgoing = avgOutgoing,
        )

        return NavigationComplexityResult(
            routeCount = routeCount,
            transitionCount = transitionCount,
            maxOutgoingTransitions = maxOutgoing,
            avgOutgoingTransitions = avgOutgoing,
            cyclicTransitionsCount = cyclicTransitions.size,
            maxNavigationDepth = maxDepth,
            complexityScore = complexityScore
        )
    }

    private fun calculateComplexityScore(
        routeCount: Int,
        transitionCount: Int,
        avgOutgoing: Double,
        maxOutgoing: Int,
        cyclicCount: Int,
        maxDepth: Int
    ): Double {
        val routeScore = minOf(routeCount / 50.0, 1.0)
        val avgOutgoingScore = minOf(avgOutgoing / 4.0, 1.0)
        val maxOutgoingScore = minOf(maxOutgoing / 8.0, 1.0)
        val cyclicScore = minOf(cyclicCount / 3.0, 1.0)
        val depthScore = minOf(maxDepth / 6.0, 1.0)

        val graphDensity = if (routeCount > 0) {
            transitionCount.toDouble() / routeCount
        } else 0.0

        val densityScore = minOf(graphDensity / 3.0, 1.0)

        return (
            avgOutgoingScore * 0.30 +
                    maxOutgoingScore * 0.20 +
                    depthScore * 0.20 +
                    cyclicScore * 0.15 +
                    densityScore * 0.10 +
                    routeScore * 0.05
            ) * 100
    }
}
