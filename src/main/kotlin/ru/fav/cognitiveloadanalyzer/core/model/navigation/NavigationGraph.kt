package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationGraph(
    val routes: List<String>,
    val transitions: List<NavigationTransition>
) {
    fun print(): String {
        val sb = StringBuilder()

        sb.appendLine("Navigation Graph:")
        sb.appendLine("Routes: ${routes.size}")
        sb.appendLine("Transitions: ${transitions.size}")

        // Группируем переходы по источнику
        val transitionsBySource = transitions.groupBy { it.from }

        routes.forEach { route ->
            val outgoing = transitionsBySource[route] ?: emptyList()
            val incoming = transitions.count { it.to == route }

            sb.appendLine()
            sb.appendLine(route)
            sb.appendLine("   Incoming: $incoming")
            sb.appendLine("   Outgoing: ${outgoing.size}")

            outgoing.forEach { transition ->
                sb.appendLine("      ${transition.to}")
            }
        }

        // Выводим циклы, если есть
        val cycles = findCycles()
        if (cycles.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("  Detected Cycles: ${cycles.size}")
            cycles.forEachIndexed { index, cycle ->
                sb.appendLine("   ${index + 1}. ${cycle.joinToString(" → ")}")
            }
        }

        return sb.toString()
    }

    private fun findCycles(): List<List<String>> {
        val cycles = mutableListOf<List<String>>()
        val visited = mutableSetOf<String>()
        val recStack = mutableSetOf<String>()

        fun dfs(node: String, path: MutableList<String>) {
            visited.add(node)
            recStack.add(node)
            path.add(node)

            val neighbors = transitions.filter { it.from == node }.map { it.to }

            for (neighbor in neighbors) {
                if (neighbor !in visited) {
                    dfs(neighbor, path)
                } else if (neighbor in recStack) {
                    // Найден цикл
                    val cycleStart = path.indexOf(neighbor)
                    if (cycleStart >= 0) {
                        val cycle = path.subList(cycleStart, path.size).toList()
                        cycles.add(cycle)
                    }
                }
            }

            path.removeAt(path.lastIndex)
            recStack.remove(node)
        }

        routes.forEach { route ->
            if (route !in visited) {
                dfs(route, mutableListOf())
            }
        }

        // Убираем дубли и нормализуем циклы
        return cycles
            .map { normalizeCycle(it) }
            .distinct()
    }

    /**
     * Нормализует цикл (начинаем с минимального элемента)
     */
    private fun normalizeCycle(cycle: List<String>): List<String> {
        if (cycle.isEmpty()) return cycle

        val minIndex = cycle.indices.minByOrNull { cycle[it] } ?: 0
        return cycle.subList(minIndex, cycle.size) + cycle.subList(0, minIndex)
    }
}