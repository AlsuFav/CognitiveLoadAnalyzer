package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion
import ru.fav.cognitiveloadanalyzer.ui.model.UiScreenResult
import ru.fav.cognitiveloadanalyzer.ui.util.RiskLevelColors
import ru.fav.cognitiveloadanalyzer.ui.util.formatValue
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea

class ScreenDetailPanel(private val project: Project) {

    private val rootPanel = JPanel(BorderLayout())

    private val titleLabel = JBLabel("Select a screen or criterion").apply {
        border = JBUI.Borders.emptyBottom(8)
        font = font.deriveFont(Font.BOLD, 13f)
    }

    // ── Вкладка 1: Criteria ───────────────────────────────────────────────
    private val criteriaArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        background = null
        border = null
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    // ── Вкладка 2: Screen Tree (ComposeUiNode.print()) ────────────────────
    private val treeArea = JTextArea().apply {
        isEditable = false
        lineWrap = false          // дерево лучше без переноса
        background = null
        border = null
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    private val tabs = JBTabbedPane()

    private val fixButton = JButton("Apply Fix").apply {
        isVisible = false
    }

    init {
        rootPanel.border = JBUI.Borders.empty(8)

        tabs.addTab("Criteria", JBScrollPane(criteriaArea))
        tabs.addTab("Screen Tree", JBScrollPane(treeArea))

        rootPanel.add(titleLabel, BorderLayout.NORTH)
        rootPanel.add(tabs, BorderLayout.CENTER)
        rootPanel.add(fixButton, BorderLayout.SOUTH)
    }

    //Показ экрана

    fun showScreen(screen: UiScreenResult) {
        titleLabel.text = screen.screenName
        titleLabel.foreground = RiskLevelColors.forRiskLevel(screen.riskLevel)

        // Вкладка Criteria
        criteriaArea.text = buildCriteriaText(screen)
        criteriaArea.caretPosition = 0

        // Вкладка Screen Tree — вот сюда идёт ComposeUiNode.print()
        treeArea.text = screen.screenTree
        treeArea.caretPosition = 0

        fixButton.isVisible = false

        // Переключаемся на Criteria при выборе экрана
        tabs.selectedIndex = 0
    }

    // ── Показ критерия ────────────────────────────────────────────────────

    fun showCriterion(criterion: CriterionResult) {
        titleLabel.text = "${criterion.criterion.id} — ${criterion.criterion.name}"
        titleLabel.foreground = RiskLevelColors.forRiskLevel(criterion.riskLevel)

        criteriaArea.text = buildSingleCriterionText(criterion)
        criteriaArea.caretPosition = 0

        // Tree не меняем — остаётся от последнего выбранного экрана
        fixButton.isVisible = false
        tabs.selectedIndex = 0
    }

    // Показ quick fix

    fun showQuickFix(fix: QuickFixSuggestion) {
        titleLabel.text = "💡 ${fix.title}"
        titleLabel.foreground = JBUI.CurrentTheme.Label.foreground()

        criteriaArea.text = fix.description
        criteriaArea.caretPosition = 0

        fixButton.actionListeners.toList()
            .forEach { fixButton.removeActionListener(it) }

        fixButton.isVisible = fix.canAutoFix
        if (fix.canAutoFix) {
            fixButton.text = "Apply Fix"
            fixButton.addActionListener { applyFix(fix) }
        }
        tabs.selectedIndex = 0
    }

    // Построение текста
    private fun buildCriteriaText(screen: UiScreenResult): String {
        val sb = StringBuilder()
        sb.appendLine("Screen     : ${screen.screenName}")
        sb.appendLine("CL         : ${screen.cognitiveLoad.formatValue()}")
        sb.appendLine("Risk       : ${screen.riskLevel}")
        sb.appendLine("File       : ${screen.filePath}")
        sb.appendLine()
        sb.appendLine("── Criteria ──────────────────────────────────")

        screen.criteria.forEach { c ->
            sb.appendLine()
            val riskMark = riskMark(c.riskLevel)
            sb.appendLine("$riskMark ${c.criterion.id}")
            sb.appendLine("  Value : ${c.formatValue()}")
            if (c.details.isNotEmpty()) {
                c.details.forEach { (k, v) ->
                    sb.appendLine("  $k: $v")
                }
            }
        }

        if (screen.quickFixes.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("── Suggestions ───────────────────────────────")
            screen.quickFixes.forEach { fix ->
                sb.appendLine("💡 ${fix.title}")
            }
        }

        return sb.toString()
    }

    private fun buildSingleCriterionText(criterion: CriterionResult): String {
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
        return sb.toString()
    }

    // ── Утилиты ───────────────────────────────────────────────────────────

    private fun riskMark(level: RiskLevel) = when (level) {
        RiskLevel.HIGH   -> "⚠"
        RiskLevel.MEDIUM -> "~"
        RiskLevel.LOW    -> "✓"
    }

    private fun applyFix(fix: QuickFixSuggestion) {
        com.intellij.openapi.ui.Messages.showInfoMessage(
            project,
            "Fix '${fix.title}'\nFile: ${fix.filePath}",
            "Fix Applied"
        )
    }

    fun getComponent() = rootPanel
}