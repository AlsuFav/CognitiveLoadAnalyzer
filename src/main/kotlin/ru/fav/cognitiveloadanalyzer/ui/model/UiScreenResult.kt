package ru.fav.cognitiveloadanalyzer.ui.model

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel

data class UiScreenResult(
    val screenName: String,
    val filePath: String,
    val cognitiveLoad: Double,
    val riskLevel: RiskLevel,
    val criteria: List<CriterionResult>,
    val quickFixes: List<QuickFixSuggestion>,
    val screenTree: String = "",
)