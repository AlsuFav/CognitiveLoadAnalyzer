package ru.fav.cognitiveloadanalyzer.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport
import ru.fav.cognitiveloadanalyzer.ui.panel.NavigationPanel
import ru.fav.cognitiveloadanalyzer.ui.panel.ScreensPanel
import ru.fav.cognitiveloadanalyzer.ui.panel.SummaryPanel
import ru.fav.cognitiveloadanalyzer.ui.service.AnalysisReportService
import ru.fav.cognitiveloadanalyzer.ui.util.formatValue
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

class CognitiveLoadToolWindowPanel(private val project: Project) {

    private val rootPanel = JPanel(BorderLayout())
    private val tabbedPane = JBTabbedPane()

    // Вкладки
    private val summaryPanel = SummaryPanel(project)
    private val screensPanel = ScreensPanel(project)
    private val navigationPanel = NavigationPanel(project)

    // Статус
    private val statusLabel = JBLabel("Ready. Run analysis to see results.")

    init {
        setupToolbar()
        setupTabs()
        setupStatusBar()
        subscribeToUpdates()
    }

    private fun setupToolbar() {
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbar.border = JBUI.Borders.empty(4)

        val runButton = JButton("▶ Run Analysis").apply {
            addActionListener { triggerAnalysis() }
        }

        val exportButton = JButton("⬇ Export").apply {
            addActionListener { exportResults() }
        }

        toolbar.add(runButton)
        toolbar.add(exportButton)
        rootPanel.add(toolbar, BorderLayout.NORTH)
    }

    private fun setupTabs() {
        tabbedPane.addTab("📊 Summary", summaryPanel.getComponent())
        tabbedPane.addTab("🖥 Screens", screensPanel.getComponent())
        tabbedPane.addTab("🗺 Navigation", navigationPanel.getComponent())
        rootPanel.add(tabbedPane, BorderLayout.CENTER)
    }

    private fun setupStatusBar() {
        val statusBar = JPanel(BorderLayout())
        statusBar.border = JBUI.Borders.customLine(
            JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground(), 1, 0, 0, 0
        )
        statusBar.add(statusLabel, BorderLayout.WEST)
        rootPanel.add(statusBar, BorderLayout.SOUTH)
    }

    private fun subscribeToUpdates() {
        AnalysisReportService.getInstance(project).addListener { report ->
            ApplicationManager.getApplication().invokeLater {
                updateUI(report)
            }
        }
    }

    private fun updateUI(report: AnalysisReport) {
        summaryPanel.update(report)
        screensPanel.update(report)
        navigationPanel.update(report)

        val time = java.text.SimpleDateFormat("HH:mm:ss").format(report.timestamp)
        statusLabel.text = "Last analysis: $time | " +
            "Screens: ${report.screens.size} | " +
            "Avg CL: ${report.averageCognitiveLoad.formatValue()}"
    }

    private fun triggerAnalysis() {
        statusLabel.text = "Analyzing..."
        // Запускает тот же движок что и StartupActivity
        ApplicationManager
            .getApplication()
            .executeOnPooledThread {
                ru.fav.cognitiveloadanalyzer.AnalyzerStartupActivity()
                    .runActivity(project)
            }
    }

    private fun exportResults() {
        // TODO: экспорт в HTML/JSON
    }

    fun getComponent() = rootPanel
}