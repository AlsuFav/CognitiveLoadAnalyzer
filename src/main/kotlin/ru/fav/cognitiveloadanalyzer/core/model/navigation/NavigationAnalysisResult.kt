package ru.fav.cognitiveloadanalyzer.core.model.navigation

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

data class NavigationAnalysisResult(
    val criterion: CriterionResult,
    val graph: NavigationGraph
)