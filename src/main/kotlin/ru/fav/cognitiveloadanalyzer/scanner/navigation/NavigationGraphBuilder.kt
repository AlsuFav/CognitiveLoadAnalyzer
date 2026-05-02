package ru.fav.cognitiveloadanalyzer.scanner.navigation

import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationTransition

class NavigationGraphBuilder {

    fun build(navigationFiles: List<KtFile>): NavigationGraph {
        val routes      = mutableSetOf<String>()
        val transitions = mutableListOf<NavigationTransition>()

        navigationFiles.forEach { file ->
            routes.addAll(RouteParser.findRoutes(file).map { it.fullName })
            transitions.addAll(EntryBuilderParser.findTransitions(file))
        }

        val entryPoints = EntryPointParser.findEntryPoints(navigationFiles)

        return NavigationGraph(
            routes = routes.toList(),
            transitions = transitions.distinct(),
            entryPoints = entryPoints
        )
    }
}