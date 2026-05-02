package ru.fav.cognitiveloadanalyzer.analyzer.screen

import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode
import ru.fav.cognitiveloadanalyzer.core.model.screen.ReusabilityResult
import ru.fav.cognitiveloadanalyzer.core.model.screen.SemanticResult

object ComposeMetrics {

    /**
     * Информационная плотность = количество конечных визуальных элементов
     * (листьев дерева, без учёта контейнеров)
     */
    fun density(root: ComposeUiNode): Int {
        return root.leafNodesCount()
    }

    /**
     * Глубина вложенности иерархии
     */
    fun depth(root: ComposeUiNode): Int {
        return root.maxDepth()
    }

    /**
     * Количество интерактивных элементов
     */
    fun clickableElementsCount(node: ComposeUiNode): Int {
        return when {
            // Если это кликабельный элемент - считаем его (не детей)
            node.isClickable() -> 1

            // Если есть дети - рекурсивно считаем
            node.children.isNotEmpty() -> node.children.sumOf { clickableElementsCount(it) }

            else -> 0
        }
    }

    /**
     * Количество анимаций
     */
    fun animationCount(node: ComposeUiNode): Int {
        return when {
            // Если это анимация - считаем
            node.isAnimation() -> 1

            // Рекурсивно в детях
            node.children.isNotEmpty() -> node.children.sumOf { animationCount(it) }

            else -> 0
        }
    }

    /**
     * Количество текстовых элементов
     */
    fun textElementsCount(node: ComposeUiNode): Int {
        return when {
            // Если это текстовый элемент без детей - считаем
            node.isTextElement() && node.children.isEmpty() -> 1

            // Если есть дети - рекурсивно считаем в детях
            node.children.isNotEmpty() -> node.children.sumOf { textElementsCount(it) }

            // Не текстовый и без детей
            else -> 0
        }
    }

    /**
     * Семантическая полнота: процент элементов без семантических метаданных
     */
    fun semanticCompleteness(root: ComposeUiNode): SemanticResult {
        var totalElements = 0
        var elementsWithSemantics = 0

        fun analyze(node: ComposeUiNode) {
            if (node.needsSemantics()) {
                totalElements++
                if (node.hasSemanticMetadata()) {
                    elementsWithSemantics++
                }
            }
            if (!node.isSelfSufficientComponent()) node.children.forEach { analyze(it) }
        }

        analyze(root)

        val missingSemanticRatio = if (totalElements > 0) {
            ((totalElements - elementsWithSemantics).toDouble() / totalElements) * 100
        } else {
            0.0
        }

        return SemanticResult(
            totalElements = totalElements,
            elementsWithSemantics = elementsWithSemantics,
            elementsWithoutSemantics = totalElements - elementsWithSemantics,
            missingSemanticRatio = missingSemanticRatio
        )
    }

    /**
     * Переиспользование компонентов
     */
    fun reusabilityMetrics(screens: List<ComposeUiNode>): ReusabilityResult {
        val usage = mutableListOf<String>()

        fun collect(node: ComposeUiNode) {
            usage.add(node.name)
            node.children.forEach { collect(it) }
        }

        screens.forEach { collect(it) }

        val usageCount = usage.groupingBy { it }.eachCount()

        val totalUsages = usage.size
        val uniqueComponents = usageCount.size

        val reusedComponents = usageCount.count { it.value > 1 }

        val reusabilityRatio = if (uniqueComponents > 0) {
            (reusedComponents.toDouble() / uniqueComponents) * 100
        } else 0.0

        return ReusabilityResult(
            totalComponents = totalUsages,
            uniqueComponents = uniqueComponents,
            reusabilityRatio = reusabilityRatio
        )
    }
}
