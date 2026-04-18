package ru.fav.cognitiveloadanalyzer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import ru.fav.cognitiveloadanalyzer.scanner.ProjectFileScanner
import ru.fav.cognitiveloadanalyzer.scanner.ProjectComposableScanner
import ru.fav.cognitiveloadanalyzer.core.engine.ScreenAnalyzerEngine
import ru.fav.cognitiveloadanalyzer.core.engine.NavigationAnalyzerEngine
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope

class AnalyzerStartupActivity : StartupActivity.DumbAware {

    private val logger = Logger.getInstance(AnalyzerStartupActivity::class.java)
    private val ANALYSIS_SCOPE = AnalysisScope.PROJECT_WIDE

    override fun runActivity(project: Project) {
        logger.info(">>> Cognitive Load Analysis STARTED <<<")
        logger.info(">>> Analysis Scope: $ANALYSIS_SCOPE <<<")

        DumbService.getInstance(project).runWhenSmart {
            ApplicationManager.getApplication().executeOnPooledThread {
                ReadAction.run<RuntimeException> {
                    analyze(project)
                }
            }
        }
    }

    private fun analyze(project: Project) {
        // ========================================
        // Сканируем все файлы
        // ========================================
        logger.info(">>> Step 1: Scanning project files <<<")
        val fileScanner = ProjectFileScanner(project)
        val allKotlinFiles = fileScanner.scanAllKotlinFiles()
        logger.info(">>> Found ${allKotlinFiles.size} Kotlin files <<<")

        if (allKotlinFiles.isEmpty()) {
            logger.warn(">>> No Kotlin files found <<<")
            return
        }

        // ========================================
        // Индексируем Composable функции
        // ========================================
        logger.info(">>> Step 2: Indexing Composable functions <<<")
        val composableScanner = ProjectComposableScanner()
        val registry = composableScanner.scanProject(allKotlinFiles)
        logger.info(">>> Indexed ${registry.size()} Composable functions <<<")

        // ========================================
        // Анализируем навигацию
        // ========================================
        logger.info(">>> Analyzing navigation <<<")
        val navigationEngine = NavigationAnalyzerEngine(logger)
        val navigationResult = navigationEngine.analyze(allKotlinFiles)

        if (navigationResult != null) {
            logger.warn("Navigation Analysis:")
            logger.warn("  ${navigationResult.criterion.id} = ${navigationResult.value} (${navigationResult.riskLevel})")
        } else {
            logger.warn("Navigation analysis skipped (no navigation files found)")
        }

        // ========================================
        // Анализируем экраны
        // ========================================
        logger.info(">>> Analyzing screens <<<")
        val screenEngine = ScreenAnalyzerEngine(registry, logger, ANALYSIS_SCOPE)
        val screenResults = screenEngine.analyzeAll(allKotlinFiles)

        logger.info(">>> Analyzed ${screenResults.size} screens <<<")

        screenResults.forEach { result ->
            logger.warn("${result.screen}: CL = ${result.cognitiveLoad}")
            result.criteria.forEach { criterion ->
                logger.warn("  ${criterion.criterion.id} = ${criterion.value} (${criterion.riskLevel})")
                criterion.details.forEach { (key, value) ->
                    logger.info("      $key: $value")
                }
            }
        }

        // ========================================
        // Общая статистика
        // ========================================
        logger.info(">>> Analysis Summary <<<")
        logger.info("  Total files: ${allKotlinFiles.size}")
        logger.info("  Composables: ${registry.size()}")
        logger.info("  Screens analyzed: ${screenResults.size}")

        val avgCL = screenResults.map { it.cognitiveLoad }.average()
        logger.warn("  Average Cognitive Load: ${"%.2f".format(avgCL)}")

        logger.info(">>> Cognitive Load Analysis FINISHED <<<")
    }
}