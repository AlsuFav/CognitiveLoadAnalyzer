package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationGraph(
    val routes: List<String>,
    val entryPoints: List<RouteEntryPoint> = emptyList(),
    val transitions: List<NavigationTransition>,
) {
    fun print(): String {
        val sb = StringBuilder()
        sb.appendLine("Navigation Graph:")
        sb.appendLine("Routes: ${routes.size}")
        sb.appendLine("Transitions: ${transitions.size}")

        val transitionsBySource = transitions.groupBy { it.from }

        routes.forEach { route ->
            val outgoing = transitionsBySource[route] ?: emptyList()
            val incoming = transitions.count { it.to == route }
            sb.appendLine()
            sb.appendLine(route)
            sb.appendLine("   Incoming: $incoming")
            sb.appendLine("   Outgoing: ${outgoing.size}")
            outgoing.forEach { sb.appendLine("      ${it.to}") }
        }

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

    // было private → теперь internal, доступно внутри модуля плагина
    fun findCycles(): List<List<String>> {
        val cycles = mutableListOf<List<String>>()
        val visited = mutableSetOf<String>()
        val recStack = mutableSetOf<String>()

        fun dfs(node: String, path: MutableList<String>) {
            visited.add(node)
            recStack.add(node)
            path.add(node)

            transitions.filter { it.from == node }.map { it.to }.forEach { neighbor ->
                if (neighbor !in visited) {
                    dfs(neighbor, path)
                } else if (neighbor in recStack) {
                    val cycleStart = path.indexOf(neighbor)
                    if (cycleStart >= 0) {
                        cycles.add(path.subList(cycleStart, path.size).toList())
                    }
                }
            }

            path.removeAt(path.lastIndex)
            recStack.remove(node)
        }

        routes.forEach { if (it !in visited) dfs(it, mutableListOf()) }

        return cycles.map { normalizeCycle(it) }.distinct()
    }

    private fun normalizeCycle(cycle: List<String>): List<String> {
        if (cycle.isEmpty()) return cycle
        val minIndex = cycle.indices.minByOrNull { cycle[it] } ?: 0
        return cycle.subList(minIndex, cycle.size) + cycle.subList(0, minIndex)
    }
}