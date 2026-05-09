package ru.fav.cognitiveloadanalyzer.ui.quickfix

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.screen.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion

object QuickFixFactory {

    private val fixProviders: List<FixProvider> = listOf(
        AnimationLoadFixProvider(),
        ContextRelevanceFixProvider(),
        SemanticCompletenessFixProvider(),
        TouchTargetsFixProvider(),
        TooManyElementsFixProvider(),
        DeepNestingFixProvider(),
    )

    fun buildFixes(result: ScreenAnalysisResult): List<QuickFixSuggestion> =
        result.criteria
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

interface FixProvider {
    fun appliesTo(criterion: CriterionResult): Boolean
    fun createFix(criterion: CriterionResult, filePath: String): QuickFixSuggestion
}
