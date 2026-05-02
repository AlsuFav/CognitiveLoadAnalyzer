package ru.fav.cognitiveloadanalyzer.core.rule.screen

import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

interface ScreensRule {
    fun evaluate(screens: List<ComposeUiNode>): CriterionResult
}