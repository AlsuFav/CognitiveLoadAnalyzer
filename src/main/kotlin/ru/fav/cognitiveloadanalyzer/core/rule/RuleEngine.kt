package ru.fav.cognitiveloadanalyzer.core.rule

import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph
import ru.fav.cognitiveloadanalyzer.core.rule.navigation.NavigationComplexityRule
import ru.fav.cognitiveloadanalyzer.core.rule.screen.*

class RuleEngine {

    private val screenRules = listOf(
        ComposeDensityScreenRule(),           // CLC1
        ComposeClickableElementsScreenRule(), // CLC4
        SemanticCompletenessScreenRule(),     // CLC5
        ComposeAnimationLoadScreenRule(),     // CLC6
        ComposeTextDensityScreenRule(),       // CLC7
        ComposeHierarchyScreenRule(),         // CLC9
        ComposeReusabilityScreenRule()        // CLC10
    )

    private val navigationRule = NavigationComplexityRule()

    fun runRulesForScreen(screenStructure: ComposeUiNode): List<CriterionResult> {
        return screenRules.map { it.evaluate(screenStructure) }
    }

    fun runRuleForNavigation(graph: NavigationGraph): CriterionResult {
        return navigationRule.evaluate(graph)
    }
}