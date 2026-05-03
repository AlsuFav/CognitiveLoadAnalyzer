package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationTransition(
    val from: String,
    val to: String,
    val type: NavigationType  // navigate, navigateClearingStack
)