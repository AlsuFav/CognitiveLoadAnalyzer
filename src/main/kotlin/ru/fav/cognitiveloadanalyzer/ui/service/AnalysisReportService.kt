package ru.fav.cognitiveloadanalyzer.ui.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.fav.cognitiveloadanalyzer.ui.model.AnalysisReport

@Service(Service.Level.PROJECT)
class AnalysisReportService {

    private var currentReport: AnalysisReport? = null
    private val listeners = mutableListOf<(AnalysisReport) -> Unit>()

    fun updateReport(report: AnalysisReport) {
        currentReport = report
        listeners.forEach { it(report) }
    }

    fun getReport(): AnalysisReport? = currentReport

    fun addListener(listener: (AnalysisReport) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (AnalysisReport) -> Unit) {
        listeners.remove(listener)
    }

    companion object {
        fun getInstance(project: Project): AnalysisReportService =
            project.service()
    }
}