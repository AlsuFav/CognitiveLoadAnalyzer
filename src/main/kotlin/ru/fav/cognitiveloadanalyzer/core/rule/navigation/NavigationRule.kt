package ru.fav.cognitiveloadanalyzer.core.rule.navigation

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph

interface NavigationRule {
    fun evaluate(graph: NavigationGraph): CriterionResult
}