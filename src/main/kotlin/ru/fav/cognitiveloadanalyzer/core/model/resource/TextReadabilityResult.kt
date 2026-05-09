package ru.fav.cognitiveloadanalyzer.core.model.resource

data class TextReadabilityResult(
        val avgLength: Double,
        val longStrings: List<ResourceString>,
        val complexityScore: Double
    )