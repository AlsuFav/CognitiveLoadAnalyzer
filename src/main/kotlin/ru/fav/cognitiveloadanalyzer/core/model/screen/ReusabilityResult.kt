package ru.fav.cognitiveloadanalyzer.core.model.screen

/**
 * Результат анализа переиспользования компонентов
 */
data class ReusabilityResult(
    val totalComponents: Int,
    val uniqueComponents: Int,
    val reusabilityRatio: Double  // 0-100%, чем меньше уникальных, тем лучше
)