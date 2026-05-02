package ru.fav.cognitiveloadanalyzer.ui.model

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

data class AnalysisReport(
    val timestamp: Long = System.currentTimeMillis(),
    val totalFiles: Int,
    val screens: List<UiScreenResult>,
    val navigation: UiNavigationResult,
    val averageCognitiveLoad: Double,
    val commonCriteria: List<CriterionResult> = emptyList(),
)