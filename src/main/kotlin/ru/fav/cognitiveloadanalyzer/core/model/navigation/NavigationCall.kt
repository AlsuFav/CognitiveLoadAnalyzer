package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationCall(
    val type: NavigationType,  // navigate, navigateClearingStack, etc.
    val destination: String  // Route.Categories, AuthRoute.Login, etc.
)