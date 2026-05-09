package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.JBSplitter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport
import ru.fav.cognitiveloadanalyzer.ui.model.QuickFixSuggestion
import ru.fav.cognitiveloadanalyzer.ui.model.UiScreenResult
import ru.fav.cognitiveloadanalyzer.ui.util.RiskLevelColors
import ru.fav.cognitiveloadanalyzer.ui.util.formatValue
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class ScreensPanel(private val project: Project) {

    private val rootPanel = JPanel(BorderLayout())
    private val treeRoot = DefaultMutableTreeNode("Screens")
    private val treeModel = DefaultTreeModel(treeRoot)
    private val tree = Tree(treeModel)
    private val detailPanel = ScreenDetailPanel(project)

    init {
        tree.cellRenderer = ScreenTreeCellRenderer()
        tree.isRootVisible = false

        // Один клик — показываем детали
        tree.addTreeSelectionListener { e ->
            val node = e.path?.lastPathComponent as? DefaultMutableTreeNode ?: return@addTreeSelectionListener
            when (val uo = node.userObject) {
                is UiScreenResult    -> detailPanel.showScreen(uo)
                is CriterionResult   -> detailPanel.showCriterion(uo)
            }
        }

        // Двойной клик — открываем файл
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
                    (node?.userObject as? UiScreenResult)?.let { openFile(it.filePath) }
                }
            }
        })

        val splitter = JBSplitter(false, 0.4f).apply {
            firstComponent = JBScrollPane(tree)
            secondComponent = detailPanel.getComponent()
        }
        rootPanel.add(splitter, BorderLayout.CENTER)
    }

    // Обновление данных
    fun update(report: AnalysisReport) {
        treeRoot.removeAllChildren()

        report.screens.forEach { screen ->
            val screenNode = DefaultMutableTreeNode(screen)

            screen.criteria.forEach { criterion ->
                val criterionNode = DefaultMutableTreeNode(criterion)
                screenNode.add(criterionNode)
            }

            treeRoot.add(screenNode)
        }

        treeModel.reload()
        expandAllNodes()
    }

    private fun expandAllNodes() {
        for (i in 0 until tree.rowCount) tree.expandRow(i)
    }

    private fun openFile(path: String) {
        val vFile = LocalFileSystem.getInstance().findFileByPath(path) ?: return
        FileEditorManager.getInstance(project).openFile(vFile, true)
    }

    fun getComponent() = rootPanel

    // Рендерер
    private inner class ScreenTreeCellRenderer : ColoredTreeCellRenderer() {

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

                // Экран
                is UiScreenResult -> {
                    icon = riskIcon(uo.riskLevel)
                    append(uo.screenName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    append(
                        "  CL: ${uo.cognitiveLoad.formatValue()}",
                        SimpleTextAttributes(
                            SimpleTextAttributes.STYLE_PLAIN,
                            RiskLevelColors.forRiskLevel(uo.riskLevel)
                        )
                    )
                    append(
                        "  ${uo.riskLevel}",
                        SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
                    )
                }

                // Критерий с деталями
                is CriterionResult -> {
                    icon = riskIcon(uo.riskLevel)
                    // ID критерия
                    append(
                        "${uo.criterion.id}: ",
                        SimpleTextAttributes.GRAYED_ATTRIBUTES
                    )
                    // Значение с цветом риска
                    append(
                        uo.formatValue(),
                        SimpleTextAttributes(
                            SimpleTextAttributes.STYLE_PLAIN,
                            RiskLevelColors.forRiskLevel(uo.riskLevel)
                        )
                    )
                    // Первая деталь в скобках (например reusabilityRatio)
                    val hint = buildHint(uo)
                    if (hint.isNotEmpty()) {
                        append(
                            "  ($hint)",
                            SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
                        )
                    }
                }
            }
        }

        // Первая "интересная" деталь для отображения рядом со значением
        private fun buildHint(criterion: CriterionResult): String {
            val details = criterion.details
            // Приоритет деталей для отображения
            val priorityKeys = listOf(
                "reusabilityRatio",
                "nestingDepth",
                "elementCount",
                "textLength",
                "routeCount"
            )
            val key = priorityKeys.firstOrNull { details.containsKey(it) }
                ?: details.keys.firstOrNull()
                ?: return ""
            return "$key: ${details[key]}"
        }

        private fun riskIcon(level: RiskLevel) = when (level) {
            RiskLevel.HIGH   -> com.intellij.icons.AllIcons.General.Warning
            RiskLevel.MEDIUM -> com.intellij.icons.AllIcons.General.Information
            RiskLevel.LOW    -> com.intellij.icons.AllIcons.General.InspectionsOK
        }
    }
}