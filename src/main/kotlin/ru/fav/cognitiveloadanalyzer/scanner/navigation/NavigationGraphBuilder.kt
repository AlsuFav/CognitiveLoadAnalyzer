package ru.fav.cognitiveloadanalyzer.scanner.navigation

import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationTransition

/**
 * Строит граф навигации
 */
class NavigationGraphBuilder {

    fun build(navigationFiles: List<KtFile>): NavigationGraph {
        val routes = mutableSetOf<String>()
        val transitions = mutableListOf<NavigationTransition>()

        navigationFiles.forEach { file ->
            // Собираем определения Route
            val routeDefinitions = RouteParser.findRoutes(file)
            routes.addAll(routeDefinitions.map { it.fullName })

            // Извлекаем переходы из *EntryBuilder функций
            val entryTransitions = EntryBuilderParser.findTransitions(file)
            transitions.addAll(entryTransitions)
        }

        return NavigationGraph(
            routes = routes.toList(),
            transitions = transitions.distinct()  // убираем дубли
        )
    }
}