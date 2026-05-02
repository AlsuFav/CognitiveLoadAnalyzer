package ru.fav.cognitiveloadanalyzer.ui.model

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

data class UiNavigationResult(
    val criterion: CriterionResult,
    val routes: List<String>,
    val transitions: List<UiTransition>,
    val cycles: List<List<String>>,
    val quickFixes: List<QuickFixSuggestion>
)