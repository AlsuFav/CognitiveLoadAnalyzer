package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.JBSplitter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion
import ru.fav.cognitiveloadanalyzer.ui.model.UiNavigationResult
import ru.fav.cognitiveloadanalyzer.ui.model.UiTransition
import ru.fav.cognitiveloadanalyzer.ui.util.RiskLevelColors
import ru.fav.cognitiveloadanalyzer.ui.util.formatValue
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class NavigationPanel(private val project: Project) {

    private val rootPanel = JPanel(BorderLayout())
    private val treeRoot = DefaultMutableTreeNode("Navigation")
    private val treeModel = DefaultTreeModel(treeRoot)
    private val tree = Tree(treeModel)
    private val detailPanel = NavigationDetailPanel()

    private val emptyLabel = JBLabel("No navigation data found").apply {
        horizontalAlignment = JBLabel.CENTER
        foreground = JBUI.CurrentTheme.Label.disabledForeground()
    }

    // Текущее состояние — показываем splitter или emptyLabel
    private var showingSplitter = false

    init {
        tree.isRootVisible = false
        tree.cellRenderer = NavigationTreeCellRenderer()

        tree.addTreeSelectionListener { e ->
            val node = e.path?.lastPathComponent as? DefaultMutableTreeNode
                ?: return@addTreeSelectionListener
            when (val uo = node.userObject) {
                is NavCriterionNode -> detailPanel.showCriterion(uo)
                is QuickFixSuggestion -> detailPanel.showFix(uo)
                is NavRouteNode -> detailPanel.showRoute(uo)
                is NavCycleNode -> detailPanel.showCycle(uo)
            }
        }

        showSplitter()
    }

    // Переключение между splitter и emptyLabel

    private fun showSplitter() {
        if (!showingSplitter) {
            rootPanel.removeAll()
            val splitter = JBSplitter(false, 0.45f).apply {
                firstComponent = JBScrollPane(tree)
                secondComponent = detailPanel.getComponent()
            }
            rootPanel.add(splitter, BorderLayout.CENTER)
            rootPanel.revalidate()
            rootPanel.repaint()
            showingSplitter = true
        }
    }

    private fun showEmpty() {
        if (showingSplitter) {
            rootPanel.removeAll()
            rootPanel.add(emptyLabel, BorderLayout.CENTER)
            rootPanel.revalidate()
            rootPanel.repaint()
            showingSplitter = false
        }
    }

    // Обновление данных
    fun update(report: AnalysisReport) {
        val nav = report.navigation
        if (nav.routes.isEmpty()) {
            showEmpty()
            return
        }

        showSplitter()
        treeRoot.removeAllChildren()
        buildTree(nav)
        treeModel.reload()
        expandAll()
    }

    // Построение дерева

    private fun buildTree(nav: UiNavigationResult) {

        // 1. Узел критерия (верхний уровень)
        val criterionNode = DefaultMutableTreeNode(
            NavCriterionNode(
                id = nav.criterion.criterion.id,
                label = "Criterion: ${nav.criterion.criterion.id}",
                value = nav.criterion.formatValue(),
                riskLevel = nav.criterion.riskLevel,
                details = nav.criterion.details
            )
        )
        treeRoot.add(criterionNode)

        // 2. Узел маршрутов
        val routesNode = DefaultMutableTreeNode("Routes (${nav.routes.size})")
        nav.routes.forEach { route ->
            val incoming = nav.transitions.count { it.to == route }
            val outgoing = nav.transitions.filter { it.from == route }
            routesNode.add(
                DefaultMutableTreeNode(
                    NavRouteNode(
                        name = route,
                        incoming = incoming,
                        outgoing = outgoing
                    )
                )
            )
        }
        treeRoot.add(routesNode)

        // 3. Узел циклов (если есть)
        if (nav.cycles.isNotEmpty()) {
            val cyclesNode = DefaultMutableTreeNode("⚠ Cycles (${nav.cycles.size})")
            nav.cycles.forEachIndexed { index, cycle ->
                cyclesNode.add(
                    DefaultMutableTreeNode(
                        NavCycleNode(
                            index = index + 1,
                            path = cycle
                        )
                    )
                )
            }
            treeRoot.add(cyclesNode)
        }

        // 4. Quick fixes
        if (nav.quickFixes.isNotEmpty()) {
            val fixesNode = DefaultMutableTreeNode("💡 Suggestions (${nav.quickFixes.size})")
            nav.quickFixes.forEach { fix ->
                fixesNode.add(DefaultMutableTreeNode(fix))
            }
            treeRoot.add(fixesNode)
        }
    }

    private fun expandAll() {
        var i = 0
        while (i < tree.rowCount) {
            tree.expandRow(i)
            i++
        }
    }

    fun getComponent() = rootPanel

    // Data-классы узлов

    data class NavCriterionNode(
        val id: String,
        val label: String,
        val value: String,
        val riskLevel: RiskLevel,
        val details: Map<String, Any>
    )

    data class NavRouteNode(
        val name: String,
        val incoming: Int,
        val outgoing: List<UiTransition>
    )

    data class NavCycleNode(
        val index: Int,
        val path: List<String>
    )

    // Рендерер

    private inner class NavigationTreeCellRenderer : ColoredTreeCellRenderer() {
        override fun customizeCellRenderer(
            tree: javax.swing.JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val node = value as? DefaultMutableTreeNode ?: return

            when (val uo = node.userObject) {

                is NavCriterionNode -> {
                    icon = when (uo.riskLevel) {
                        RiskLevel.HIGH -> com.intellij.icons.AllIcons.General.Warning
                        RiskLevel.MEDIUM -> com.intellij.icons.AllIcons.General.Information
                        RiskLevel.LOW -> com.intellij.icons.AllIcons.General.InspectionsOK
                    }
                    append(uo.label, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    append(
                        " = ${uo.value}",
                        SimpleTextAttributes(
                            SimpleTextAttributes.STYLE_PLAIN,
                            RiskLevelColors.forRiskLevel(uo.riskLevel)
                        )
                    )
                    append("  [${uo.riskLevel}]", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
                }

                is NavRouteNode -> {
                    icon = com.intellij.icons.AllIcons.Nodes.Package
                    append(uo.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    append(
                        "  ↓${uo.incoming} ↑${uo.outgoing.size}",
                        SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
                    )
                }

                is NavCycleNode -> {
                    icon = com.intellij.icons.AllIcons.General.Warning
                    append(
                        "${uo.index}. ${uo.path.joinToString(" → ")}",
                        SimpleTextAttributes(
                            SimpleTextAttributes.STYLE_PLAIN,
                            RiskLevelColors.HIGH
                        )
                    )
                }

                is QuickFixSuggestion -> {
                    icon = com.intellij.icons.AllIcons.Actions.IntentionBulb
                    append(uo.title, SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES)
                }

                is String -> {
                    append(uo, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
            }
        }
    }
}