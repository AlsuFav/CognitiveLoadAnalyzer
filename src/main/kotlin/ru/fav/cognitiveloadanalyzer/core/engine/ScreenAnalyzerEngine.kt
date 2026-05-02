package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposableFunctionParser
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposeTreeBuilder
import ru.fav.cognitiveloadanalyzer.analyzer.CognitiveLoadCalculator
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.screen.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine
import ru.fav.cognitiveloadanalyzer.scanner.ProjectComposableScanner

class ScreenAnalyzerEngine(
    files: List<KtFile>,
    private val logger: Logger,
    private val scope: AnalysisScope = AnalysisScope.SAME_PACKAGE
) {

    private val ruleEngine = RuleEngine()
    private val screens = mutableListOf<ComposeUiNode>()

    init {
        val composableScanner = ProjectComposableScanner()
        val registry = composableScanner.scanProject(files)

        files.map { file ->
            val functions = ComposableFunctionParser.find(file)
            functions.firstOrNull {
                it.name?.endsWith("Screen") == true
            }?.let { screenComposable ->
                val node = ComposeTreeBuilder(registry, file, scope).build(screenComposable)
                screens.add(node)
            }
        }
    }

    fun analyzeAll(): List<CriterionResult> {
        return ruleEngine.runRulesForAllScreens(screens)
    }

    fun analyzeAllDetailed(): List<ScreenAnalysisResult> {
        return screens.map { analyzeScreen(it) }
    }

    private fun analyzeScreen(screenStructure: ComposeUiNode): ScreenAnalysisResult {
        val results = ruleEngine.runRulesForScreen(screenStructure)
        val cl = CognitiveLoadCalculator.calculate(results)

        logger.warn(screenStructure.print())

        return ScreenAnalysisResult(
            screen = screenStructure.name,
            criteria = results,
            cognitiveLoad = cl
        )
    }
}