package ru.fav.cognitiveloadanalyzer.analyzer.compose

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
        return (if (node.isClickable()) 1 else 0) +
                node.children.sumOf { clickableElementsCount(it) }
    }

    /**
     * Количество анимаций
     */
    fun animationCount(node: ComposeUiNode): Int {
        return (if (node.isAnimation()) 1 else 0) +
                node.children.sumOf { animationCount(it) }
    }
}