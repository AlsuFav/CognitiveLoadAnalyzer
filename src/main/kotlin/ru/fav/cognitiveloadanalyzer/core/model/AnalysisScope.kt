package ru.fav.cognitiveloadanalyzer.core.model

/**
 * Режим анализа иерархии Composable функций
 */
enum class AnalysisScope {
    /**
     * Раскрывать только функции из того же пакета, что и анализируемый экран
     * Пример: если экран в com.example.feature.home, 
     * раскрываются только функции из com.example.feature.home
     */
    SAME_PACKAGE,
    
    /**
     * Раскрывать функции из любого пакета проекта
     * (исключая только внешние библиотеки)
     */
    PROJECT_WIDE,
    
    /**
     * Не раскрывать вложенные функции вообще
     * (только локальная структура внутри одной функции)
     */
    LOCAL_ONLY
}