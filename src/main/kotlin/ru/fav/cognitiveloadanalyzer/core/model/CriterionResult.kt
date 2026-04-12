package ru.fav.cognitiveloadanalyzer.core.model

data class CriterionResult(
    val criterion: CognitiveCriterion,
    val value: Double,
    val riskLevel: RiskLevel
)