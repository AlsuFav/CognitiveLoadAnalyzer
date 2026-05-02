package ru.fav.cognitiveloadanalyzer.core.model

data class CriterionResult(
    val criterion: CognitiveCriterion,
    val value: Double,           // normalized 0..100
    val rawValue: Double,        // реальное значение
    val riskLevel: RiskLevel,
    val details: Map<String, Any> = emptyMap(),
)