package ru.fav.cognitiveloadanalyzer.analyzer.compose

data class ComposeUiNode(
    val name: String,
    val depth: Int = 0,
    val children: MutableList<ComposeUiNode> = mutableListOf()
) {

    /**
     * Общее количество всех узлов (включая контейнеры)
     */
    fun totalChildrenCount(): Int =
        children.size + children.sumOf { it.totalChildrenCount() }

    /**
     * Количество только конечных элементов (листьев дерева)
     * Это те элементы, которые реально видны пользователю
     */
    fun leafNodesCount(): Int {
        return when {
            // Самодостаточные компоненты считаются как 1, даже если есть дети
            isSelfSufficientComponent() -> 1

            // Конечные элементы (без детей)
            children.isEmpty() -> {
                if (isNonVisualElement()) 0 else 1
            }

            // Контейнеры — считаем детей
            else -> children.sumOf { it.leafNodesCount() }
        }
    }

    fun maxDepth(): Int =
        if (children.isEmpty()) depth
        else children.maxOf { it.maxDepth() }

    fun deepCopy(newDepth: Int = this.depth): ComposeUiNode {
        return ComposeUiNode(
            name = this.name,
            depth = newDepth,
            children = this.children.map { it.deepCopy(newDepth + (it.depth - this.depth)) }.toMutableList()
        )
    }

    fun print(indent: Int = 0): String {
        val sb = StringBuilder()
        val marker = if (children.isEmpty()) "element" else "container"
        sb.appendLine("${"  ".repeat(indent)}└── $marker $name")
        children.forEach { sb.append(it.print(indent + 1)) }
        return sb.toString()
    }
}