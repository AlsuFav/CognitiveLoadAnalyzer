package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine
import ru.fav.cognitiveloadanalyzer.scanner.navigation.NavigationGraphBuilder

class NavigationAnalyzerEngine(
    private val logger: Logger,
) {

    private val ruleEngine = RuleEngine()

    fun analyze(files: List<KtFile>): CriterionResult? {
        val navigationFiles = files
            .filter { file ->
                file.name.contains("Navigation", ignoreCase = true) ||
                        file.name.contains("EntryBuilder", ignoreCase = true) ||
                        file.name.contains("Route", ignoreCase = true) ||
                        file.packageFqName.asString().contains("navigation")
                }
            .ifEmpty { return null }

        val graph = NavigationGraphBuilder().build(navigationFiles)

        logger.warn(graph.print())

        return ruleEngine.runRuleForNavigation(graph)
    }
}