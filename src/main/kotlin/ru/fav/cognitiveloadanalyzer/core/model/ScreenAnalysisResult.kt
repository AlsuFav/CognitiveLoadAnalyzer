package ru.fav.cognitiveloadanalyzer.core.model

data class ScreenAnalysisResult(
    val screen: String,
    val criteria: List<CriterionResult>,
    val cognitiveLoad: Double
)