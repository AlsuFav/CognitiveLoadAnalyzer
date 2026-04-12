package ru.fav.cognitiveloadanalyzer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.scanner.ProjectComposableScanner
import ru.fav.cognitiveloadanalyzer.core.engine.ScreenAnalyzerEngine
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope

class AnalyzerStartupActivity : StartupActivity.DumbAware {

    private val logger = Logger.getInstance(AnalyzerStartupActivity::class.java)
    private val SKIP_DIRS = setOf("build", ".gradle", ".idea", "node_modules", "out")

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
        logger.info(">>> Step 1: Scanning project for Composable functions <<<")
        val scanner = ProjectComposableScanner(project)
        val registry = scanner.scanProject()
        logger.info(">>> Found ${registry.size()} Composable functions <<<")

        val engine = ScreenAnalyzerEngine(registry, logger, ANALYSIS_SCOPE)

        val psiManager = PsiManager.getInstance(project)
        val baseDir = project.basePath?.let {
            LocalFileSystem.getInstance().findFileByPath(it)
        }

        if (baseDir == null) {
            logger.warn(">>> Project base directory not found <<<")
            return
        }

        val kotlinFiles = mutableListOf<VirtualFile>()
        VfsUtilCore.visitChildrenRecursively(baseDir, object : VirtualFileVisitor<Void>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.isDirectory && file.name in SKIP_DIRS) {
                    return false
                }
                if (!file.isDirectory && file.extension == "kt") {
                    kotlinFiles.add(file)
                }
                return true
            }
        })

        logger.info(">>> Step 2: Analyzing ${kotlinFiles.size} Kotlin files <<<")

        kotlinFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) as? KtFile ?: return@forEach

            val result = engine.analyze(psiFile)

            if (result != null) {
                logger.warn("${result.screen}: CL = ${result.cognitiveLoad}")
                result.criteria.forEach {
                    logger.warn("${it.criterion.id} = ${it.value} (${it.riskLevel})")
                }
            }
        }

        logger.info(">>> Cognitive Load Analysis FINISHED <<<")
    }
}