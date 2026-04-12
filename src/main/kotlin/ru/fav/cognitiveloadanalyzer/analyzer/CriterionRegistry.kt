package ru.fav.cognitiveloadanalyzer.analyzer

import ru.fav.cognitiveloadanalyzer.core.model.CognitiveCriterion

object CriterionRegistry {

    val CLC1 = CognitiveCriterion(
        id = "CLC1",
        name = "Information Density",
        weight = 0.35
    )

    val CLC4 = CognitiveCriterion(
        id = "CLC4",
        name = "Touch Target Analysis",
        weight = 0.25
    )

    val CLC6 = CognitiveCriterion(
        id = "CLC6",
        name = "Animation and Transition Load",
        weight = 0.15
    )

    val CLC9 = CognitiveCriterion(
        id = "CLC9",
        name = "Hierarchy Complexity",
        weight = 0.25
    )

    val all = listOf(CLC1, CLC4, CLC6, CLC9)
}