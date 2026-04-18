package ru.fav.cognitiveloadanalyzer.scanner

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile

/**
 * Сканирует проект и собирает все Kotlin файлы
 */
class ProjectFileScanner(private val project: Project) {
    
    private val SKIP_DIRS = setOf("build", ".gradle", ".idea", "node_modules", "out")
    
    /**
     * Собирает все .kt файлы в проекте
     */
    fun scanAllKotlinFiles(): List<KtFile> {
        val psiManager = PsiManager.getInstance(project)
        
        val baseDir = project.basePath?.let {
            LocalFileSystem.getInstance().findFileByPath(it)
        } ?: return emptyList()
        
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
        
        return kotlinFiles.mapNotNull { psiManager.findFile(it) as? KtFile }
    }
}