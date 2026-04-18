package ru.fav.cognitiveloadanalyzer.core.model.screen

/**
 * Результат анализа семантической полноты
 */
data class SemanticResult(
    val totalElements: Int,
    val elementsWithSemantics: Int,
    val elementsWithoutSemantics: Int,
    val missingSemanticRatio: Double  // 0-100%
)