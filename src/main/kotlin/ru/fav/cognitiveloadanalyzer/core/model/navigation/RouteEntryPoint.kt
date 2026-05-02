package ru.fav.cognitiveloadanalyzer.core.model.navigation

data class RouteEntryPoint(
    val route: String,
    val type: EntryPointType,
    val label: String? = null   // текст пункта меню если есть
)