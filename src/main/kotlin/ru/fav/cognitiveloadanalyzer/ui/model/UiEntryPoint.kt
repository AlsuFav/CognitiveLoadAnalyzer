package ru.fav.cognitiveloadanalyzer.ui.model

data class UiEntryPoint(
    val route: String,
    val type: String,        // "BOTTOM_NAVIGATION", "DRAWER" и т.д.
    val label: String?
)