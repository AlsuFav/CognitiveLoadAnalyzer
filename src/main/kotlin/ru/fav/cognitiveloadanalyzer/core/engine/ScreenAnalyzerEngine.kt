package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.analyzer.CognitiveLoadCalculator
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.screen.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine
import ru.fav.cognitiveloadanalyzer.scanner.ProjectComposableScanner
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposableFunctionParser
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposeTreeBuilder

class ScreenAnalyzerEngine(
    files: List<KtFile>,
    private val logger: Logger,
    private val scope: AnalysisScope = AnalysisScope.SAME_PACKAGE
) {
    private val ruleEngine = RuleEngine()

   private val screens = mutableListOf<Pair<ComposeUiNode, String>>()

    init {
        val registry = ProjectComposableScanner().scanProject(files)

        files.forEach { file ->
            ComposableFunctionParser.find(file)
                .firstOrNull { it.name?.endsWith("Screen") == true }
                ?.let { screenComposable ->
                    val node = ComposeTreeBuilder(registry, file, scope).build(screenComposable)
                    val path = file.virtualFile.path
                    screens.add(node to path)
                }
        }
    }

    fun analyzeAll(): List<CriterionResult> =
        ruleEngine.runRulesForAllScreens(screens.map { it.first })

    fun analyzeAllDetailed(): List<ScreenAnalysisResult> =
        screens.map { (node, path) -> analyzeScreen(node, path) }

    private fun analyzeScreen(node: ComposeUiNode, filePath: String): ScreenAnalysisResult {
        val results = ruleEngine.runRulesForScreen(node)
        val cl = CognitiveLoadCalculator.calculate(results)
        logger.info(node.print())
        return ScreenAnalysisResult(
            screen = node.name,
            filePath = filePath,
            criteria = results,
            cognitiveLoad = cl,
            screenTree = node.print(),
        )
    }
}