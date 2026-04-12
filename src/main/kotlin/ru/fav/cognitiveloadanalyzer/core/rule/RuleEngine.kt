package ru.fav.cognitiveloadanalyzer.core.rule

import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.rule.screen.ComposeAnimationLoadScreenRule
import ru.fav.cognitiveloadanalyzer.core.rule.screen.ComposeClickableElementsScreenRule
import ru.fav.cognitiveloadanalyzer.core.rule.screen.ComposeDensityScreenRule
import ru.fav.cognitiveloadanalyzer.core.rule.screen.ComposeHierarchyScreenRule

class RuleEngine {

    private val screenRules = listOf(
        ComposeDensityScreenRule(),
        ComposeHierarchyScreenRule(),
        ComposeClickableElementsScreenRule(),
        ComposeAnimationLoadScreenRule()
    )

    fun runRulesForScreen(screenStructure: ComposeUiNode): List<CriterionResult> {
        return screenRules.map { it.evaluate(screenStructure) }
    }
}