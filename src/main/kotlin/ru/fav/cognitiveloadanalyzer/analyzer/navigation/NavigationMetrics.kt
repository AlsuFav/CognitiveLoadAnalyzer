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
        val cyclicTransitions = findCycles(graph)
        
        // Вычисляем максимальную глубину навигации
        val maxDepth = calculateMaxDepth(graph)
        
        // Complexity Score (0-100)
        val complexityScore = calculateComplexityScore(
            routeCount = routeCount,
            transitionCount = transitionCount,
            maxOutgoing = maxOutgoing,
            cyclicCount = cyclicTransitions.size,
            maxDepth = maxDepth
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
    
    private fun findCycles(graph: NavigationGraph): List<List<String>> {
        val cycles = mutableListOf<List<String>>()
        val visited = mutableSetOf<String>()
        val stack = mutableSetOf<String>()
        
        // DFS для поиска циклов
        fun dfs(node: String, path: List<String>) {
            if (node in stack) {
                // Найден цикл
                val cycleStart = path.indexOf(node)
                if (cycleStart >= 0) {
                    cycles.add(path.subList(cycleStart, path.size))
                }
                return
            }
            
            if (node in visited) return
            
            visited.add(node)
            stack.add(node)
            
            val neighbors = graph.transitions.filter { it.from == node }.map { it.to }
            neighbors.forEach { neighbor ->
                dfs(neighbor, path + neighbor)
            }
            
            stack.remove(node)
        }
        
        graph.routes.forEach { route ->
            if (route !in visited) {
                dfs(route, listOf(route))
            }
        }
        
        return cycles
    }
    
    private fun calculateMaxDepth(graph: NavigationGraph): Int {
        // BFS для поиска максимальной глубины от стартового экрана
        val startRoute = graph.routes.firstOrNull() ?: return 0
        
        val depths = mutableMapOf<String, Int>()
        val queue = ArrayDeque<Pair<String, Int>>()
        queue.add(startRoute to 0)
        depths[startRoute] = 0
        
        while (queue.isNotEmpty()) {
            val (current, depth) = queue.removeFirst()
            
            val neighbors = graph.transitions.filter { it.from == current }.map { it.to }
            neighbors.forEach { neighbor ->
                if (neighbor !in depths || depths[neighbor]!! > depth + 1) {
                    depths[neighbor] = depth + 1
                    queue.add(neighbor to depth + 1)
                }
            }
        }
        
        return depths.values.maxOrNull() ?: 0
    }
    
    private fun calculateComplexityScore(
        routeCount: Int,
        transitionCount: Int,
        maxOutgoing: Int,
        cyclicCount: Int,
        maxDepth: Int
    ): Double {
        // Нормализованные компоненты (0-1)
        val routeScore = minOf(routeCount / 20.0, 1.0)  // >20 экранов = max
        val transitionScore = minOf(transitionCount / 50.0, 1.0)  // >50 переходов = max
        val outgoingScore = minOf(maxOutgoing / 10.0, 1.0)  // >10 исходящих = max
        val cyclicScore = minOf(cyclicCount / 5.0, 1.0)  // >5 циклов = max
        val depthScore = minOf(maxDepth / 8.0, 1.0)  // >8 уровней = max
        
        // Взвешенная сумма (веса можно настроить)
        return (routeScore * 0.2 +
                transitionScore * 0.3 +
                outgoingScore * 0.2 +
                cyclicScore * 0.15 +
                depthScore * 0.15) * 100.0
    }
}
