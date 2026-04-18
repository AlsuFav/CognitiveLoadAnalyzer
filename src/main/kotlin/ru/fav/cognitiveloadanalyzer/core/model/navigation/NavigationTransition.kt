package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationTransition(
    val from: String,
    val to: String,
    val type: String  // navigate, navigateClearingStack, navigateBack
)