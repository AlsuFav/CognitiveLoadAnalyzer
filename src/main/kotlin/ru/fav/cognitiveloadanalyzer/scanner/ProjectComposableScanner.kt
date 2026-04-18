package ru.fav.cognitiveloadanalyzer.scanner

import org.jetbrains.kotlin.psi.KtFile
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposableFunctionParser
import ru.fav.cognitiveloadanalyzer.scanner.screen.ComposableRegistry

/**
 * Индексирует все Composable функции из файлов
 */
class ProjectComposableScanner {

    fun scanProject(files: List<KtFile>): ComposableRegistry {
        val registry = ComposableRegistry()

        files.forEach { ktFile ->
            val composables = ComposableFunctionParser.find(ktFile)
            composables.forEach { registry.register(it) }
        }

        return registry
    }
}