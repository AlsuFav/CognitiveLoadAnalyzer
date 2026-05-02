package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion
import ru.fav.cognitiveloadanalyzer.ui.quickfix.QuickFixFactory
import ru.fav.cognitiveloadanalyzer.ui.util.RiskLevelColors
import ru.fav.cognitiveloadanalyzer.ui.util.formatValue
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea

class CommonCriterionDetailPanel(private val project: Project) {

    private val rootPanel = JPanel(BorderLayout())

    private val titleLabel = JBLabel("Select a criterion").apply {
        border = JBUI.Borders.emptyBottom(8)
        font = font.deriveFont(Font.BOLD, 13f)
    }

    private val contentArea = JTextArea().apply {
        isEditable = false
        lineWrap   = true
        wrapStyleWord = true
        background = null
        border     = null
        font       = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    private val fixButton = JButton("Apply Fix").apply {
        isVisible = false
    }

    init {
        rootPanel.border = JBUI.Borders.empty(8)
        rootPanel.add(titleLabel,               BorderLayout.NORTH)
        rootPanel.add(JBScrollPane(contentArea), BorderLayout.CENTER)
        rootPanel.add(fixButton,                BorderLayout.SOUTH)
    }

    // Показ критерия

    fun showCriterion(criterion: CriterionResult) {
        titleLabel.text      = "${criterion.criterion.id} — ${criterion.criterion.name}"
        titleLabel.foreground = RiskLevelColors.forRiskLevel(criterion.riskLevel)

        contentArea.text = buildText(criterion)
        contentArea.caretPosition = 0

        // Ищем подходящие fixes
        val fixes = QuickFixFactory.buildNavFixes(criterion)
            .plus(
                // Пробуем screen-fixes через заглушку ScreenAnalysisResult
                emptyList<QuickFixSuggestion>()
            )

        setupFixButton(fixes.firstOrNull())
    }

    // Текст деталей

    private fun buildText(criterion: CriterionResult): String {
        val sb = StringBuilder()

        sb.appendLine("Criterion  : ${criterion.criterion.id}")
        sb.appendLine("Name       : ${criterion.criterion.name}")
        sb.appendLine("Value      : ${criterion.formatValue()}")
        sb.appendLine("Risk Level : ${criterion.riskLevel}")

        if (criterion.details.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("── Details ───────────────────────────────────")
            criterion.details.forEach { (k, v) ->
                sb.appendLine("  $k: $v")
            }
        }

        // Подсказки по уровню риска
        val hint = riskHint(criterion)
        if (hint.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("── Recommendation ────────────────────────────")
            sb.appendLine(hint)
        }

        return sb.toString()
    }

    private fun riskHint(criterion: CriterionResult): String = when {
        criterion.riskLevel == RiskLevel.LOW -> "✓ No action needed."

        criterion.criterion.id.contains("CLC10") -> """
            Reusability ratio is low.
            • Extract repeated composables into shared components
            • Create a design-system module with common UI elements
            • Use slots pattern for flexible reuse
        """.trimIndent()

        criterion.criterion.id.contains("NAV") -> """
            Navigation complexity is high.
            • Split into nested navigation graphs
            • Reduce number of top-level destinations
        """.trimIndent()

        else -> """
            Risk level is ${criterion.riskLevel}.
            Review the details above and consider refactoring.
        """.trimIndent()
    }

    // Fix кнопка

    private fun setupFixButton(fix: QuickFixSuggestion?) {
        // Сбрасываем старые listeners
        fixButton.actionListeners.toList()
            .forEach { fixButton.removeActionListener(it) }

        if (fix == null) {
            fixButton.isVisible = false
            return
        }

        fixButton.isVisible  = fix.canAutoFix
        fixButton.text       = "Apply Fix: ${fix.title}"
        fixButton.addActionListener {
            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                fix.description,
                "Fix: ${fix.title}"
            )
        }
    }

    fun getComponent() = rootPanel
}