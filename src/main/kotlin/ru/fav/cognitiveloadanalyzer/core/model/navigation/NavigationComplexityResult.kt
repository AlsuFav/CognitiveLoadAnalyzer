package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationComplexityResult(
    val routeCount: Int,
    val transitionCount: Int,
    val maxOutgoingTransitions: Int,
    val avgOutgoingTransitions: Double,
    val cyclicTransitionsCount: Int,
    val maxNavigationDepth: Int,
    val complexityScore: Double  // 0-100
)