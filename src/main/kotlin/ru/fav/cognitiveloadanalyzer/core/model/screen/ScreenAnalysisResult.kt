package ru.fav.cognitiveloadanalyzer.core.model.screen

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

data class ScreenAnalysisResult(
    val screen: String,
    val criteria: List<CriterionResult>,
    val cognitiveLoad: Double
)