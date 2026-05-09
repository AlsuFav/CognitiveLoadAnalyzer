package ru.fav.cognitiveloadanalyzer.core.model

import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

data class CriterionResult(
    val criterion: CognitiveCriterion,
    val value: Double,           // normalized 0..100
    val rawValue: Double,        // реальное значение
    val riskLevel: RiskLevel,
    val quickFixSuggestion: QuickFixSuggestion? = null,
    val details: Map<String, Any> = emptyMap(),
)