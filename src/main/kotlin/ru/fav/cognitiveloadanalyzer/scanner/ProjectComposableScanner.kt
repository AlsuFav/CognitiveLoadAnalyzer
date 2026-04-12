package ru.fav.cognitiveloadanalyzer.scanner

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile

/**
 * Сканирует весь проект и находит все Composable функции
 */
class ProjectComposableScanner(private val project: Project) {
    
    private val SKIP_DIRS = setOf("build", ".gradle", ".idea", "node_modules", "out")
    
    fun scanProject(): ComposableRegistry {
        val registry = ComposableRegistry()
        val psiManager = PsiManager.getInstance(project)
        
        val baseDir = project.basePath?.let {
            LocalFileSystem.getInstance().findFileByPath(it)
        } ?: return registry
        
        // Собираем все .kt файлы
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
        
        // Парсим и регистрируем все Composable функции
        kotlinFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) as? KtFile ?: return@forEach
            val composables = ComposableFunctionParser.find(psiFile)
            composables.forEach { registry.register(it) }
        }
        
        return registry
    }
}