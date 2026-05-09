package ru.fav.cognitiveloadanalyzer.core.rule

import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationGraph
import ru.fav.cognitiveloadanalyzer.core.model.resource.ResourceString
import ru.fav.cognitiveloadanalyzer.core.rule.navigation.NavigationComplexityRule
import ru.fav.cognitiveloadanalyzer.core.rule.resources.TextReadabilityRule
import ru.fav.cognitiveloadanalyzer.core.rule.screen.*

class RuleEngine {

    private val screenRules = listOf(
        ComposeDensityScreenRule(),           // CLC1
        ComposeClickableElementsScreenRule(), // CLC4
        SemanticCompletenessScreenRule(),     // CLC5
        ComposeAnimationLoadScreenRule(),     // CLC6
        ComposeTextDensityScreenRule(),       // CLC7
        ComposeHierarchyScreenRule(),         // CLC9
    )

    private val screensRule = listOf(ComposeReusabilityRule())

    private val navigationRule = NavigationComplexityRule()

    private val resourcesRule = TextReadabilityRule()

    fun runRulesForScreen(screenStructure: ComposeUiNode): List<CriterionResult> {
        return screenRules.map { it.evaluate(screenStructure) }
    }

    fun runRulesForAllScreens(screens: List<ComposeUiNode>): List<CriterionResult> {
        return screensRule.map { it.evaluate(screens) }
    }

    fun runRuleForNavigation(graph: NavigationGraph): CriterionResult {
        return navigationRule.evaluate(graph)
    }

    fun runRuleForResources(strings: List<ResourceString>): CriterionResult {
        return resourcesRule.evaluate(strings)
    }
}