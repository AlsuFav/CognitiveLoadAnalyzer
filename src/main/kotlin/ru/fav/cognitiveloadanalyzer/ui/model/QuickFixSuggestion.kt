package ru.fav.cognitiveloadanalyzer.ui.model

data class QuickFixSuggestion(
    val title: String,
    val description: String,
    val actionId: String,          // ID для применения fix
    val filePath: String? = null,
    val canAutoFix: Boolean = false
)