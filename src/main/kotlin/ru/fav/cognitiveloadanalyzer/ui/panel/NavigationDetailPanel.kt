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

    fun showEntryPoint(node: NavigationPanel.NavEntryPointNode) {
        titleLabel.text = node.route
        titleLabel.foreground = JBUI.CurrentTheme.Label.foreground()
        contentArea.text = buildString {
            appendLine("Route      : ${node.route}")
            appendLine("Entry via  : ${typeLabel(node.type)}")
            if (node.label != null) {
                appendLine("Label      : ${node.label}")
            }
            appendLine()
            appendLine("── What this means ──────────────────")
            appendLine(entryPointExplanation(node.type))
        }
        contentArea.caretPosition = 0
    }

    // Клик по группе "Bottom Navigation" — показываем общее описание типа
    fun showEntryGroup(node: NavigationPanel.NavEntryGroupNode) {
        titleLabel.text = node.label
        titleLabel.foreground = JBUI.CurrentTheme.Label.foreground()
        contentArea.text = buildString {
            appendLine("Entry point type: ${node.label}")
            appendLine()
            appendLine(groupExplanation(node.iconKey))
        }
        contentArea.caretPosition = 0
    }

    private fun groupExplanation(iconKey: String) = when (iconKey) {
        "bottom_nav" ->
            "These routes are accessible from the Bottom Navigation Bar.\n" +
                    "They are top-level destinations — the user can reach them at any time."
        "drawer" ->
            "These routes are accessible from the Navigation Drawer.\n" +
                    "Drawer destinations are typically top-level screens."
        "top_bar" ->
            "These routes are triggered from the Top App Bar.\n" +
                    "Usually Settings, Profile, or contextual actions."
        "deep_link" ->
            "These routes can be opened directly via a deep link URL.\n" +
                    "Make sure they handle missing or invalid arguments gracefully."
        "start" ->
            "This is the initial screen of the navigation graph.\n" +
                    "It is shown first when the app or sub-graph starts."
        else -> "Click on individual routes to see details."
    }

    private fun typeLabel(type: String) = when (type) {
        "BOTTOM_NAVIGATION" -> "Bottom Navigation Bar"
        "DRAWER"            -> "Navigation Drawer"
        "TOP_BAR"           -> "Top App Bar"
        "DEEP_LINK"         -> "Deep Link"
        "START_DESTINATION" -> "Start Destination"
        else                -> type
    }

    private fun entryPointExplanation(type: String) = when (type) {
        "BOTTOM_NAVIGATION" ->
            "User can reach this screen from the bottom navigation bar.\n" +
                    "It is a top-level destination — avoid deep back stacks here."
        "DRAWER" ->
            "User can reach this screen from the navigation drawer.\n" +
                    "Drawer items are typically top-level destinations."
        "TOP_BAR" ->
            "User can navigate here via the top app bar action.\n" +
                    "Usually used for settings or profile screens."
        "DEEP_LINK" ->
            "This screen can be opened directly via a deep link URL.\n" +
                    "Ensure it handles missing arguments gracefully."
        "START_DESTINATION" ->
            "This is the initial screen shown when the nav graph starts.\n" +
                    "It cannot be popped from the back stack."
        else -> "Entry point type: $type"
    }

    fun getComponent() = rootPanel
}