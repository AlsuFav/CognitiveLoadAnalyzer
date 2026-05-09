package ru.fav.cognitiveloadanalyzer.ui.panel

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextArea
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport

class ResourcesPanel(private val project: Project) {

    private val root = JPanel(BorderLayout())
    private val area = JTextArea()

    init {
        area.isEditable = false
        root.add(JBScrollPane(area), BorderLayout.CENTER)
    }

    fun update(report: AnalysisReport) {
        val c = report.resources.criterion

        val ids = c.details["long string ids"] as? List<*> ?: emptyList<Any>()

        area.text = buildString {
            appendLine("Criterion: ${c.criterion.id}")
            appendLine("Value: ${c.value}")
            appendLine("Risk: ${c.riskLevel}")
            c.details
                .filter {  it.key != "long string ids" }
                .forEach { (k, v) -> appendLine("$k: $v") }
            appendLine()
            if (ids.isNotEmpty()) {
                appendLine("Too long strings:")
                ids.forEach {
                    appendLine("• $it")
                }
                appendLine()
                appendLine("── How to fix ───────────────────────")
                appendLine("• Split long paragraphs into smaller chunks")
                appendLine("• Reduce sentence complexity")
                appendLine("• Replace technical wording with plain language")
                appendLine("• Move secondary details to expandable sections")
            } else {
                appendLine( "✓ No action needed.")
            }
        }
    }

    fun getComponent() = root
}