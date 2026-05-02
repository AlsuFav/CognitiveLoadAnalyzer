package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import ru.fav.cognitiveloadanalyzer.core.model.CriterionResult
import ru.fav.cognitiveloadanalyzer.core.model.RiskLevel
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport
import ru.fav.cognitiveloadanalyzer.ui.util.RiskLevelColors
import ru.fav.cognitiveloadanalyzer.ui.util.formatValue
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.border.TitledBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class SummaryPanel(private val project: Project) {

    private val rootPanel    = JPanel(BorderLayout())
    private val metricsPanel = JPanel(GridLayout(0, 4, 8, 8))

    // Карточки
    private val totalFilesCard   = MetricCard("Files Scanned", "—")
    private val totalScreensCard = MetricCard("Screens Analyzed", "—")
    private val avgClCard        = MetricCard("Avg Cognitive Load", "—")
    private val highRiskCard     = MetricCard("High Risk Screens", "—")

    // Таблица commonCriteria
    private val tableColumns = arrayOf("Criterion", "Value", "Risk")
    private val tableModel = object : DefaultTableModel(tableColumns, 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val table = JBTable(tableModel).apply {
        setShowGrid(false)
        intercellSpacing = java.awt.Dimension(0, 0)
        rowHeight = 24
        columnModel.getColumn(0).preferredWidth = 100
        columnModel.getColumn(1).preferredWidth = 100
        columnModel.getColumn(2).preferredWidth = 80
        columnModel.getColumn(2).cellRenderer = RiskCellRenderer()
        selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val row = selectedRow
                if (row >= 0) currentCriteria.getOrNull(row)?.let { c ->
                    detailPanel.showCriterion(c)
                }
            }
        }
    }

    // Текущие критерии
    private var currentCriteria: List<CriterionResult> = emptyList()

    // Панель деталей справа
    private val detailPanel = CommonCriterionDetailPanel(project)

    // Сплиттер
    private val splitter = JBSplitter(false, 0.35f).apply {
        firstComponent  = JBScrollPane(table)
        secondComponent = detailPanel.getComponent()
    }
    private val tablePanel = JPanel(BorderLayout()).apply {
        border = TitledBorder("Common Criteria (across all screens)")
        add(splitter, BorderLayout.CENTER)
    }

    init {
        rootPanel.border = JBUI.Borders.empty(8)

        metricsPanel.border = TitledBorder("Overview")
        metricsPanel.add(totalFilesCard.panel)
        metricsPanel.add(totalScreensCard.panel)
        metricsPanel.add(avgClCard.panel)
        metricsPanel.add(highRiskCard.panel)

        rootPanel.add(metricsPanel, BorderLayout.NORTH)
        rootPanel.add(tablePanel,   BorderLayout.CENTER)
    }

    // Обновление
    fun update(report: AnalysisReport) {
        updateCards(report)
        updateTable(report)
    }

    private fun updateCards(report: AnalysisReport) {
        totalFilesCard.setValue(report.totalFiles.toString())
        totalScreensCard.setValue(report.screens.size.toString())

        val avg = report.averageCognitiveLoad
        avgClCard.setValue(avg.formatValue())
        avgClCard.setColor(RiskLevelColors.forCognitiveLoad(avg))

        val highRisk = report.screens.count { it.riskLevel == RiskLevel.HIGH }
        highRiskCard.setValue(highRisk.toString())
        highRiskCard.setColor(
            if (highRisk > 0) RiskLevelColors.HIGH else RiskLevelColors.LOW
        )
    }

    private fun updateTable(report: AnalysisReport) {
        currentCriteria  = report.commonCriteria
        tableModel.rowCount = 0

        currentCriteria.forEach { criterion ->
            tableModel.addRow(
                arrayOf(
                    criterion.criterion.id,
                    criterion.formatValue(),
                    criterion.riskLevel.name
                )
            )
        }

        // Выбираем первую строку автоматически
        if (tableModel.rowCount > 0) {
            table.setRowSelectionInterval(0, 0)
        }
    }

    fun getComponent() = rootPanel

    // Рендерер Risk
    private inner class RiskCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable, value: Any?,
            isSelected: Boolean, hasFocus: Boolean,
            row: Int, column: Int
        ): Component {
            val c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
            )
            val risk = RiskLevel.entries.firstOrNull { it.name == value?.toString() }
            if (!isSelected && risk != null) {
                c.foreground = RiskLevelColors.forRiskLevel(risk)
            }
            return c
        }
    }

    // Карточка метрики
    private inner class MetricCard(title: String, initialValue: String) {
        val panel = JPanel(BorderLayout())
        private val titleLabel = JBLabel(title)
        private val valueLabel = JBLabel(initialValue)

        init {
            panel.border = JBUI.Borders.customLine(
                JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()
            )
            titleLabel.font = titleLabel.font.deriveFont(Font.PLAIN, 11f)
            valueLabel.font = valueLabel.font.deriveFont(Font.BOLD, 24f)

            val inner = JPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(8)
                add(titleLabel, BorderLayout.NORTH)
                add(valueLabel, BorderLayout.CENTER)
            }
            panel.add(inner)
        }

        fun setValue(text: String) { valueLabel.text = text }
        fun setColor(color: java.awt.Color) { valueLabel.foreground = color }
    }
}