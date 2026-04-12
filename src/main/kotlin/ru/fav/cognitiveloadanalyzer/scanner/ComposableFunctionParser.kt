package ru.fav.cognitiveloadanalyzer.scanner

import org.jetbrains.kotlin.psi.*

object ComposableFunctionParser {

    fun find(file: KtFile): List<KtNamedFunction> {
        val topLevel = file.declarations.filterIsInstance<KtNamedFunction>()

        val inClasses = file.declarations
            .filterIsInstance<KtClassOrObject>()
            .flatMap { it.declarations.filterIsInstance<KtNamedFunction>() }

        return (topLevel + inClasses).filter { it.isComposable() }
    }

    private fun KtNamedFunction.isComposable(): Boolean {
        return annotationEntries.any {
            it.shortName?.asString() == "Composable"
        }
    }
}