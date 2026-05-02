package ru.fav.cognitiveloadanalyzer.ui.util

import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult

fun Double.formatValue(): String = "%.2f".format(this)

fun CriterionResult.formatValue() = this.value.formatValue()