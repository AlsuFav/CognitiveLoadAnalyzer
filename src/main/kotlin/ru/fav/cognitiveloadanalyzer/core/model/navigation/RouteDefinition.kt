package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class RouteDefinition(
    val fullName: String,      // Route.Categories, AuthRoute.Login
    val simpleName: String,     // Categories, Login
    val hasParameters: Boolean  // true для Route.PoseDetails(poseId)
)
