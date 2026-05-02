package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine
import ru.fav.cognitiveloadanalyzer.scanner.navigation.NavigationGraphBuilder

class NavigationAnalyzerEngine(
    files: List<KtFile>,
    private val logger: Logger,
) {
    private val navigationFiles: List<KtFile> = files.filter { file ->
        file.name.contains("Navigation", ignoreCase = true) ||
                file.name.contains("EntryBuilder", ignoreCase = true) ||
                file.name.contains("Route", ignoreCase = true) ||
                file.packageFqName.asString().contains("navigation")
    }

    private val ruleEngine = RuleEngine()

    fun analyze(): NavigationAnalysisResult {
        val graph = NavigationGraphBuilder().build(navigationFiles)
        logger.info(graph.print())

        val criterion = ruleEngine.runRuleForNavigation(graph)

        return NavigationAnalysisResult(
            criterion = criterion,
            graph = graph
        )
    }
}