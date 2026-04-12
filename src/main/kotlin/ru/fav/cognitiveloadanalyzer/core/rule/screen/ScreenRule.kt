package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.analyzer.compose.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

interface ScreenRule {
    fun evaluate(screenStructure: ComposeUiNode): CriterionResult
}