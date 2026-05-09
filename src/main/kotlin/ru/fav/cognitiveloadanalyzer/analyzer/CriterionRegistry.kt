package ru.fav.cognitiveloadanalyzer.analyzer

import ru.fav.cognitiveloadanalyzer.core.model.CognitiveCriterion

object CriterionRegistry {

    val CLC1 = CognitiveCriterion(
        id = "CLC1",
        name = "Information Density",
        weight = 0.20
    )

    val CLC2 = CognitiveCriterion(
        id = "CLC2",
        name = "Text Readability Index",
        weight = 0.10
    )

    val CLC4 = CognitiveCriterion(
        id = "CLC4",
        name = "Touch Target Analysis",
        weight = 0.10
    )

    val CLC5 = CognitiveCriterion(
        id = "CLC5",
        name = "Semantic Completeness",
        weight = 0.10
    )

    val CLC6 = CognitiveCriterion(
        id = "CLC6",
        name = "Animation and Transition Load",
        weight = 0.10
    )

    val CLC7 = CognitiveCriterion(
        id = "CLC7",
        name = "Text Density",
        weight = 0.10
    )

    val CLC9 = CognitiveCriterion(
        id = "CLC9",
        name = "Hierarchy Complexity",
        weight = 0.10
    )

    val CLC10 = CognitiveCriterion(
        id = "CLC10",
        name = "Reusability",
        weight = 0.10
    )

    val CLC11 = CognitiveCriterion(
        id = "CLC11",
        name = "Navigation Complexity",
        weight = 0.10
    )

    val all = listOf(CLC1, CLC4, CLC5, CLC6, CLC7, CLC9, CLC10, CLC11)

    val totalWeight = all.sumOf { it.weight }

    val screenWeight = listOf(
        CLC1, CLC4, CLC5, CLC6, CLC7, CLC9
    ).sumOf { it.weight }
}