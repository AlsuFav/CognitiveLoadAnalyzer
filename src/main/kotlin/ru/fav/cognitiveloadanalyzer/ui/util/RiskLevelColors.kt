package ru.fav.cognitiveloadanalyzer.ui.util

import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import java.awt.Color

object RiskLevelColors {
    val HIGH   = Color(204, 0,   0)
    val MEDIUM = Color(204, 120, 0)
    val LOW    = Color(0,   153, 0)

    fun forRiskLevel(level: RiskLevel) = when (level) {
        RiskLevel.HIGH   -> HIGH
        RiskLevel.MEDIUM -> MEDIUM
        RiskLevel.LOW    -> LOW
    }

    fun forCognitiveLoad(cl: Double) = when {
        cl >= 70.0 -> HIGH
        cl >= 40.0 -> MEDIUM
        else      -> LOW
    }
}