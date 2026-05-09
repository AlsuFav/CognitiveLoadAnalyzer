package ru.fav.cognitiveloadanalyzer.core.engine

import com.intellij.psi.xml.XmlFile
import ru.fav.cognitiveloadanalyzer.core.model.resource.ResourceString
import ru.fav.cognitiveloadanalyzer.core.model.resource.ResourcesAnalysisResult
import ru.fav.cognitiveloadanalyzer.core.rule.RuleEngine

class ResourcesAnalyzerEngine(
    private val xmlFiles: List<XmlFile>
) {
    private val strings: List<ResourceString> = xmlFiles
        .filter { it.name == "strings.xml" }
        .flatMap { file ->
            file.rootTag
                ?.findSubTags("string")
                ?.mapNotNull { tag ->
                    val id = tag.getAttributeValue("name")
                    val value = tag.value.text.trim()

                    if (id != null && value.isNotBlank()) {
                        ResourceString(id, value)
                    } else null
                }
                ?: emptyList()
        }

    private val ruleEngine = RuleEngine()

    fun analyze(): ResourcesAnalysisResult {
        val criterion = ruleEngine.runRuleForResources(strings)

        return ResourcesAnalysisResult(
            criterion = criterion,
        )
    }
}