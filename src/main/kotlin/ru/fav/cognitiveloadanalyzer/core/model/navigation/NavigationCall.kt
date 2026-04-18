package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class NavigationCall(
    val type: String,  // navigate, navigateClearingStack, etc.
    val destination: String  // Route.Categories, AuthRoute.Login, etc.
)