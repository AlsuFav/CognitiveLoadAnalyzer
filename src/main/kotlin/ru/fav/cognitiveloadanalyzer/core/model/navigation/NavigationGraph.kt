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
            outgoing.forEach { sb.appendLine("      ${it.to} ${it.type}") }
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

    fun calculateMaxDepth(): Int {
        val startRoute = entryPoints
            .firstOrNull { it.type == EntryPointType.START_DESTINATION }
            ?.route
            ?: routes.firstOrNull()
            ?: return 0

        val adjacency = transitions
            .groupBy { it.from }
            .mapValues { (_, transitions) -> transitions.map { it.to } }

        fun dfs(
            node: String,
            visited: Set<String>
        ): Int {
            if (node in visited) return 0

            val neighbors = adjacency[node].orEmpty()

            if (neighbors.isEmpty()) return 0

            return 1 + (neighbors.maxOfOrNull { neighbor ->
                dfs(neighbor, visited + node)
            } ?: 0)
        }

        return dfs(startRoute, emptySet())
    }

    fun findCycles(): List<List<String>> {
        val cycles = mutableListOf<List<String>>()

        val adjacency = transitions
            .filter { it.type != NavigationType.CLEARING_STACK }
            .groupBy { it.from }
            .mapValues { (_, edges) -> edges.map { it.to } }

        fun dfs(
            node: String,
            path: MutableList<String>,
            stack: MutableSet<String>
        ) {
            if (node in stack) {
                val cycleStart = path.indexOf(node)
                if (cycleStart >= 0) {
                    cycles.add(path.subList(cycleStart, path.size).toList())
                }
                return
            }

            stack.add(node)
            path.add(node)

            adjacency[node].orEmpty().forEach { neighbor ->
                dfs(neighbor, path.toMutableList(), stack.toMutableSet())
            }
        }

        routes.forEach { route ->
            dfs(route, mutableListOf(), mutableSetOf())
        }

        return cycles
            .map(::normalizeCycle)
            .distinct()
    }

    private fun normalizeCycle(cycle: List<String>): List<String> {
        if (cycle.isEmpty()) return cycle
        val minIndex = cycle.indices.minByOrNull { cycle[it] } ?: 0
        return cycle.subList(minIndex, cycle.size) + cycle.subList(0, minIndex)
    }
}