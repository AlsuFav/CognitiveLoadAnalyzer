package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion
import ru.fav.cognitiveloadanalyzer.ui.util.RiskLevelColors
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JTextArea

class NavigationDetailPanel {

    private val rootPanel = JPanel(BorderLayout())

    private val titleLabel = JBLabel("Select a node").apply {
        border = JBUI.Borders.emptyBottom(8)
        font = font.deriveFont(Font.BOLD, 13f)
    }

    private val contentArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        background = null
        border = null
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    init {
        rootPanel.border = JBUI.Borders.empty(8)
        rootPanel.add(titleLabel, BorderLayout.NORTH)
        rootPanel.add(JBScrollPane(contentArea), BorderLayout.CENTER)
    }

    fun showCriterion(node: NavigationPanel.NavCriterionNode) {
        titleLabel.text = node.label
        titleLabel.foreground = RiskLevelColors.forRiskLevel(node.riskLevel)
        contentArea.text = buildString {
            appendLine("Criterion : ${node.id}")
            appendLine("Value     : ${node.value}")
            appendLine("Risk      : ${node.riskLevel}")
            appendLine()
            appendLine("── Details ──────────────────────────")
            node.details.forEach { (k, v) -> appendLine("$k: $v") }
        }
        contentArea.caretPosition = 0
    }

    fun showRoute(node: NavigationPanel.NavRouteNode) {
        titleLabel.text = node.name
        titleLabel.foreground = JBUI.CurrentTheme.Label.foreground()
        contentArea.text = buildString {
            appendLine("Route: ${node.name}")
            appendLine()
            appendLine("Incoming transitions: ${node.incoming}")
            appendLine("Outgoing transitions: ${node.outgoing.size}")
            if (node.outgoing.isNotEmpty()) {
                appendLine()
                appendLine("── Navigates to ─────────────────────")
                node.outgoing.forEach { t -> appendLine("  → ${t.to}") }
            }
        }
        contentArea.caretPosition = 0
    }

    fun showCycle(node: NavigationPanel.NavCycleNode) {
        titleLabel.text = "⚠ Cycle ${node.index}"
        titleLabel.foreground = RiskLevelColors.HIGH
        contentArea.text = buildString {
            appendLine("Cyclic navigation path detected:")
            appendLine()
            appendLine(node.path.joinToString(" → "))
            appendLine()
            appendLine("── How to fix ───────────────────────")
            appendLine("• Use popUpTo() with inclusive=true to clear back stack")
            appendLine("• Replace cycle with explicit back navigation (.navigateUp())")
            appendLine("• Review if both directions are intentional")
        }
        contentArea.caretPosition = 0
    }

    fun showFix(fix: QuickFixSuggestion) {
        titleLabel.text = "💡 ${fix.title}"
        titleLabel.foreground = JBUI.CurrentTheme.Label.foreground()
        contentArea.text = fix.description
        contentArea.caretPosition = 0
    }

    fun getComponent() = rootPanel
}