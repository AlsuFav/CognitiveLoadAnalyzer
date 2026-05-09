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
                is NavEntryPointNode  -> detailPanel.showEntryPoint(uo)
                is NavEntryGroupNode  -> detailPanel.showEntryGroup(uo)
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

        // 2. Entry Points
        if (nav.entryPoints.isNotEmpty()) {
            val entryNode = DefaultMutableTreeNode(
                "Entry Points (${nav.entryPoints.size})"
            )
            // Группируем по типу
            nav.entryPoints
                .groupBy { it.type }
                .forEach { (type, points) ->
                    val typeNode = DefaultMutableTreeNode(
                        NavEntryGroupNode(typeLabel(type), iconForType(type))
                    )
                    points.forEach { ep ->
                        typeNode.add(
                            DefaultMutableTreeNode(
                                NavEntryPointNode(
                                    route = ep.route,
                                    type  = ep.type,
                                    label = ep.label
                                )
                            )
                        )
                    }
                    entryNode.add(typeNode)
                }
            treeRoot.add(entryNode)
        }

        // 3. Узел маршрутов
        val routesNode = DefaultMutableTreeNode("Routes (${nav.routes.size})")
        nav.routes.forEach { route ->
            val incoming = nav.transitions.count { it.to == route } + nav.entryPoints.count { it.route == route }
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

        // 4. Узел циклов (если есть)
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

    data class NavEntryGroupNode(
        val label: String,
        val iconKey: String
    )

    data class NavEntryPointNode(
        val route: String,
        val type: String,
        val label: String?
    )

    private fun typeLabel(type: String) = when (type) {
        "BOTTOM_NAVIGATION" -> "⬇ Bottom Navigation"
        "DRAWER"            -> "☰ Navigation Drawer"
        "TOP_BAR"           -> "⬆ Top Bar"
        "DEEP_LINK"         -> "🔗 Deep Links"
        "START_DESTINATION" -> "▶ Start Destination"
        else                -> type
    }

    private fun iconForType(type: String) = when (type) {
        "BOTTOM_NAVIGATION" -> "bottom_nav"
        "DRAWER"            -> "drawer"
        "TOP_BAR"           -> "top_bar"
        "DEEP_LINK"         -> "deep_link"
        "START_DESTINATION" -> "start"
        else                -> "unknown"
    }

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

                is String -> {
                    append(uo, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }

                is NavEntryGroupNode -> {
                    icon = com.intellij.icons.AllIcons.Nodes.Folder
                    append(uo.label, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }

                is NavEntryPointNode -> {
                    icon = when (uo.type) {
                        "BOTTOM_NAVIGATION" -> com.intellij.icons.AllIcons.Nodes.Plugin
                        "DRAWER"            -> com.intellij.icons.AllIcons.Actions.ListFiles
                        "TOP_BAR"           -> com.intellij.icons.AllIcons.Actions.MoveToTopLeft
                        "DEEP_LINK"         -> com.intellij.icons.AllIcons.Ide.Link
                        "START_DESTINATION" -> com.intellij.icons.AllIcons.RunConfigurations.TestState.Run
                        else                -> com.intellij.icons.AllIcons.Nodes.Package
                    }
                    append(uo.route, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    if (uo.label != null) {
                        append(
                            "  \"${uo.label}\"",
                            SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
                        )
                    }
                }
            }
        }
    }
}