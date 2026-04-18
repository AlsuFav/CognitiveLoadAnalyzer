package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposableFunctionParser
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposableRegistry
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposeTreeBuilder
import ru.fav.cognitiveloadanalyzer.analyzer.CognitiveLoadCalculator
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope
import ru.fav.cognitiveloadanalyzer.core.model.screen.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine

class ScreenAnalyzerEngine(
    private val registry: ComposableRegistry?,
    private val logger: Logger,
    private val scope: AnalysisScope = AnalysisScope.SAME_PACKAGE
) {

    private val ruleEngine = RuleEngine()

    fun analyze(file: KtFile): ScreenAnalysisResult? {
        val functions = ComposableFunctionParser.find(file)
        val screenComposable = functions.firstOrNull {
            it.name?.endsWith("Screen") == true
        } ?: return null

        val screenStructure = ComposeTreeBuilder(registry, file, scope).build(screenComposable)
        val results = ruleEngine.runRulesForScreen(screenStructure)
        val cl = CognitiveLoadCalculator.calculate(results)

        logger.warn(screenStructure.print())

        return ScreenAnalysisResult(
            screen = screenComposable.name!!,
            criteria = results,
            cognitiveLoad = cl
        )
    }

    fun analyzeAll(files: List<KtFile>): List<ScreenAnalysisResult> {
        return files.mapNotNull { analyze(it) }
    }
}