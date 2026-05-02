package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.screen.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

object QuickFixFactory {

    // Реестр
    private val fixProviders: List<FixProvider> = listOf(
        TooManyElementsFixProvider(),
        DeepNestingFixProvider(),
        LongTextFixProvider()
    )

    fun buildFixes(result: ScreenAnalysisResult): List<QuickFixSuggestion> =
        result.criteria
            .filter { it.riskLevel == RiskLevel.HIGH }
            .flatMap { criterion ->
                fixProviders
                    .filter { it.appliesTo(criterion) }
                    .map { it.createFix(criterion, "") }
            }

    fun buildNavFixes(criterion: CriterionResult): List<QuickFixSuggestion> =
        fixProviders
            .filter { it.appliesTo(criterion) }
            .map { it.createFix(criterion, "") }
}

// Интерфейс провайдера

interface FixProvider {
    fun appliesTo(criterion: CriterionResult): Boolean
    fun createFix(criterion: CriterionResult, filePath: String): QuickFixSuggestion
}

// Конкретные провайдеры

class TooManyElementsFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "C1" && criterion.riskLevel == RiskLevel.HIGH

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Split screen into sections",
            description = """
                Screen has ${criterion.value} interactive elements (recommended: ≤7).
                
                Suggestions:
                • Group related elements into LazyColumn sections
                • Move secondary actions to a bottom sheet or overflow menu
                • Consider splitting into multiple screens
            """.trimIndent(),
            actionId = "C1_SPLIT_SCREEN",
            filePath = filePath,
            canAutoFix = false   // станет true когда напишем PSI-рефакторинг
        )
}

class DeepNestingFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "C2" && criterion.riskLevel == RiskLevel.HIGH

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Reduce composable nesting depth",
            description = """
                Nesting depth is ${criterion.value} (recommended: ≤4).
                
                Suggestions:
                • Extract nested composables into separate functions
                • Use Modifier chains instead of wrapper composables
                • Flatten Row/Column hierarchies
            """.trimIndent(),
            actionId = "C2_REDUCE_NESTING",
            filePath = filePath,
            canAutoFix = false
        )
}

class LongTextFixProvider : FixProvider {
    override fun appliesTo(criterion: CriterionResult) =
        criterion.criterion.id == "C3" && criterion.riskLevel == RiskLevel.HIGH

    override fun createFix(criterion: CriterionResult, filePath: String) =
        QuickFixSuggestion(
            title = "Shorten text content",
            description = """
                Average text length is ${criterion.value} chars (recommended: ≤60).
                
                Suggestions:
                • Use concise labels
                • Move long descriptions to tooltips or info dialogs
                • Apply text truncation with ellipsis for non-critical content
            """.trimIndent(),
            actionId = "C3_SHORTEN_TEXT",
            filePath = filePath,
            canAutoFix = false
        )
}