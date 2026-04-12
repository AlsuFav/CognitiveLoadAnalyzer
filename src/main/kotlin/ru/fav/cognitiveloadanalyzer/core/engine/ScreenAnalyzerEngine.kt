package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.scanner.ComposableFunctionParser
import ru.fav.cognitiveloadanalyzer.scanner.ComposableRegistry
import ru.fav.cognitiveloadanalyzer.analyzer.compose.ComposeTreeBuilder
import ru.fav.cognitiveloadanalyzer.analyzer.CognitiveLoadCalculator
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope
import ru.fav.cognitiveloadanalyzer.core.model.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine

class ScreenAnalyzerEngine(
    private val registry: ComposableRegistry? = null,
    private val logger: Logger,
    private val scope: AnalysisScope = AnalysisScope.SAME_PACKAGE
) {

    private val ruleEngine = RuleEngine()

    fun analyze(file: PsiFile): ScreenAnalysisResult? {
        val ktFile = file as? KtFile ?: return null
        val functions = ComposableFunctionParser.find(ktFile)
        val screenComposable = functions.firstOrNull {
            it.name?.endsWith("Screen") == true
        } ?: return null
        val screenStructure = ComposeTreeBuilder(registry, ktFile, scope).build(screenComposable)

        val results = ruleEngine.runRulesForScreen(screenStructure)

        val cl = CognitiveLoadCalculator.calculate(results)

        logger.warn(screenStructure.print())
        return ScreenAnalysisResult(
            screen = screenComposable.name!!,
            criteria = results,
            cognitiveLoad = cl
        )
    }
}