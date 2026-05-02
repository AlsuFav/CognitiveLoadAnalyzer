package ru.fav.cognitiveloadanalyzer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import ru.fav.cognitiveloadanalyzer.analyzer.CognitiveLoadCalculator
import ru.fav.cognitiveloadanalyzer.core.engine.NavigationAnalyzerEngine
import ru.fav.cognitiveloadanalyzer.core.engine.ScreenAnalyzerEngine
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.model.screen.ScreenAnalysisResult
import ru.fav.cognitiveloadanalyzer.scanner.ProjectFileScanner
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport
import ru.fav.cognitiveloadanalyzer.ui.model.UiNavigationResult
import ru.fav.cognitiveloadanalyzer.ui.model.UiScreenResult
import ru.fav.cognitiveloadanalyzer.ui.model.UiTransition
import ru.fav.cognitiveloadanalyzer.ui.quickfix.QuickFixFactory
import ru.fav.cognitiveloadanalyzer.ui.service.AnalysisReportService

class AnalyzerStartupActivity : StartupActivity.DumbAware {

    private val logger = Logger.getInstance(AnalyzerStartupActivity::class.java)
    private val analysisScope = AnalysisScope.PROJECT_WIDE

    override fun runActivity(project: Project) {
        logger.info(">>> Cognitive Load Analysis STARTED <<<")
        DumbService.getInstance(project).runWhenSmart {
            ApplicationManager.getApplication().executeOnPooledThread {
                ReadAction.run<RuntimeException> { analyze(project) }
            }
        }
    }

    fun analyze(project: Project) {
        // 1. Сканируем файлы
        logger.info(">>> Step 1: Scanning project files <<<")
        val fileScanner = ProjectFileScanner(project)
        val allKotlinFiles = fileScanner.scanAllKotlinFiles()
        logger.info(">>> Found ${allKotlinFiles.size} Kotlin files <<<")

        if (allKotlinFiles.isEmpty()) {
            logger.warn(">>> No Kotlin files found <<<")
            return
        }

        // 2. Навигация
        logger.info(">>> Step 2: Analyzing navigation <<<")
        val navigationEngine = NavigationAnalyzerEngine(allKotlinFiles, logger)
        val navigationAnalysis = navigationEngine.analyze()   // NavigationAnalysisResult?

        // 3. Экраны: детальный анализ
        logger.info(">>> Step 3: Analyzing screens <<<")
        val screenEngine = ScreenAnalyzerEngine(allKotlinFiles, logger, analysisScope)
        val screenDetailedResults = screenEngine.analyzeAllDetailed()
        logger.info(">>> Analyzed ${screenDetailedResults.size} screens <<<")

        // 4. Общий анализ по всем экранам
        val commonCriteria = screenEngine.analyzeAll()

        logger.info(">>> Common criteria across all screens <<<")
        commonCriteria.forEach { criterion ->
            logger.info("  ${criterion.criterion.id} = ${criterion.value} (${criterion.riskLevel})")
            criterion.details.forEach { (k, v) -> logger.info("    $k: $v") }
        }

        // 5. Строим отчёт и публикуем
        val avgCL = CognitiveLoadCalculator.calculate(listOf(navigationAnalysis.criterion) + screenDetailedResults.flatMap { it.criteria } + commonCriteria)
        val report = buildReport(
            totalFiles = allKotlinFiles.size,
            screenResults = screenDetailedResults,
            navigationAnalysis = navigationAnalysis,
            commonCriteria = commonCriteria,
            averageCognitiveLoad = avgCL,
        )

        AnalysisReportService.getInstance(project).updateReport(report)

        logger.info(">>> Analysis FINISHED | Files: ${allKotlinFiles.size} | Screens: ${screenDetailedResults.size} | Avg CL: ${"%.2f".format(report.averageCognitiveLoad)} <<<")
    }

    // Построение отчёта

    private fun buildReport(
        totalFiles: Int,
        screenResults: List<ScreenAnalysisResult>,
        navigationAnalysis: NavigationAnalysisResult,
        commonCriteria: List<CriterionResult>,
        averageCognitiveLoad: Double,
    ): AnalysisReport {

        val uiScreens = screenResults.map { result ->
            UiScreenResult(
                screenName = result.screen,
                filePath = result.filePath,
                cognitiveLoad = result.cognitiveLoad,
                riskLevel = cognitiveLoadToRisk(result.cognitiveLoad),
                criteria = result.criteria,
                quickFixes = QuickFixFactory.buildFixes(result),
                screenTree = result.screenTree,
            )
        }

        val uiNavigation = UiNavigationResult(
            criterion = navigationAnalysis.criterion,
            routes = navigationAnalysis.graph.routes,
            transitions = navigationAnalysis.graph.transitions.map { t ->
                UiTransition(from = t.from, to = t.to)
            },
            cycles = navigationAnalysis.graph.findCycles(),
            quickFixes = QuickFixFactory.buildNavFixes(navigationAnalysis.criterion)
        )

        return AnalysisReport(
            totalFiles = totalFiles,
            screens = uiScreens,
            navigation = uiNavigation,
            averageCognitiveLoad = averageCognitiveLoad,
            commonCriteria = commonCriteria
        )
    }

    private fun cognitiveLoadToRisk(cl: Double) = when {
        cl >= 70.0 -> RiskLevel.HIGH
        cl >= 40.0 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }
}