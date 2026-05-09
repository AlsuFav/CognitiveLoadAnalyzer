package ru.fav.cognitiveloadanalyzer.core.rule.resources

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.resource.ResourceString

interface ResourcesRule {
    fun evaluate(strings: List<ResourceString>): CriterionResult
}