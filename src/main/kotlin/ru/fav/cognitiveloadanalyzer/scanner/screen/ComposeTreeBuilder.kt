package ru.fav.cognitiveloadanalyzer.scanner.screen

import org.jetbrains.kotlin.psi.*
import ru.fav.cognitiveloadanalyzer.core.model.AnalysisScope
import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode

class ComposeTreeBuilder(
    private val registry: ComposableRegistry? = null,
    private val contextFile: KtFile? = null,
    private val scope: AnalysisScope = AnalysisScope.PROJECT_WIDE
) {

    private val visitedFunctions = mutableSetOf<String>()
    private val rootPackage: String = contextFile?.packageFqName?.asString() ?: ""

    fun build(function: KtNamedFunction): ComposeUiNode {
        visitedFunctions.clear()
        val root = ComposeUiNode(
            name = function.name ?: "Root",
            depth = 0,
            sourceFile = contextFile,
        )
        function.fqName?.asString()?.let { visitedFunctions.add(it) }

        function.bodyExpression?.let {
            extract(it, root, 1)
        }

        return root
    }

    private fun extract(
        expression: KtExpression,
        parent: ComposeUiNode,
        depth: Int
    ) {
        when (expression) {

            is KtBlockExpression -> {
                expression.statements.forEach {
                    extract(it, parent, depth)
                }
            }

            is KtCallExpression -> {
                val name = expression.calleeExpression?.text ?: return

                if (name == "items" || name == "item") {
                    handleLazyListItems(expression, parent, depth)
                    return
                }

                if (isUiComposable(name)) {
                    val parameters = extractParameters(expression)

                    val node = ComposeUiNode(
                        name = name,
                        depth = depth,
                        parameters = parameters,
                        psiElement = expression,
                        sourceFile = expression.containingKtFile,
                    )
                    parent.children.add(node)

                    processLambdas(expression, node, depth + 1)
                    expandComposableFunction(name, node, depth + 1)
                } else {
                    processLambdas(expression, parent, depth)
                }
            }

            is KtIfExpression -> {
                extractMaxBranch(expression, parent, depth)
            }

            is KtWhenExpression -> {
                extractMaxWhenBranch(expression, parent, depth)
            }

            is KtForExpression -> {
                expression.body?.let { extract(it, parent, depth) }
            }

            is KtLambdaExpression -> {
                expression.bodyExpression?.let { extract(it, parent, depth) }
            }

            is KtReturnExpression -> {
                expression.returnedExpression?.let { extract(it, parent, depth) }
            }
        }
    }

    private fun extractMaxBranch(
        expression: KtIfExpression,
        parent: ComposeUiNode,
        depth: Int
    ) {
        val branches = listOfNotNull(expression.then, expression.`else`)
        if (branches.isEmpty()) return

        val branchNodes = branches.map { branch ->
            val tempParent = ComposeUiNode("if-temp", depth = depth)
            extract(branch, tempParent, depth)
            tempParent
        }

        val maxBranch = branchNodes.maxByOrNull { it.leafNodesCount() }

        maxBranch?.children?.forEach { child ->
            parent.children.add(child.deepCopy(child.depth))
        }
    }

    private fun extractMaxWhenBranch(
        expression: KtWhenExpression,
        parent: ComposeUiNode,
        depth: Int
    ) {
        val entries = expression.entries
        if (entries.isEmpty()) return

        val branchNodes = entries.mapIndexed { index, entry ->
            val tempParent = ComposeUiNode("when-temp-$index", depth = depth)
            entry.expression?.let { expr ->
                extract(expr, tempParent, depth)
            }
            tempParent
        }

        val maxBranch = branchNodes.maxByOrNull { it.leafNodesCount() }

        maxBranch?.children?.forEach { child ->
            parent.children.add(child.deepCopy(child.depth))
        }
    }

    private fun expandComposableFunction(
        name: String,
        parent: ComposeUiNode,
        depth: Int
    ) {
        if (scope == AnalysisScope.LOCAL_ONLY) return
        if (registry == null || contextFile == null) return

        val function = registry.resolve(name, contextFile) ?: return
        val fqn = function.fqName?.asString() ?: return

        if (fqn in visitedFunctions) {
            return
        }

        if (!shouldExpand(function)) {
            return
        }

        visitedFunctions.add(fqn)

        function.bodyExpression?.let { body ->
            extract(body, parent, depth)
        }

        visitedFunctions.remove(fqn)
    }

    private fun shouldExpand(function: KtNamedFunction): Boolean {
        val functionPackage = function.containingKtFile.packageFqName.asString()

        return when (scope) {
            AnalysisScope.LOCAL_ONLY -> false

            AnalysisScope.SAME_PACKAGE -> {
                functionPackage == rootPackage ||
                        functionPackage.startsWith("$rootPackage.")
            }

            AnalysisScope.PROJECT_WIDE -> {
                !isExternalLibrary(functionPackage)
            }
        }
    }

    private fun isExternalLibrary(packageName: String): Boolean {
        return packageName.startsWith("androidx.compose.") ||
                packageName.startsWith("androidx.") ||
                packageName.startsWith("kotlin.") ||
                packageName.startsWith("java.") ||
                packageName.startsWith("android.")
    }

    /**
     * Избегаем дублирования лямбд
     */
    private fun processLambdas(
        call: KtCallExpression,
        parent: ComposeUiNode,
        depth: Int
    ) {
        val processedLambdas = mutableSetOf<KtLambdaExpression>()

        // 1. Обрабатываем trailing lambdas
        call.lambdaArguments.forEach { lambdaArg ->
            lambdaArg.getLambdaExpression()?.let { lambda ->
                if (lambda !in processedLambdas) {
                    processedLambdas.add(lambda)
                    lambda.bodyExpression?.let { body ->
                        extract(body, parent, depth)
                    }
                }
            }
        }

        // 2. Обрабатываем named lambda parameters (только те, что ещё не обработаны)
        call.valueArguments.forEach { arg ->
            val expr = arg.getArgumentExpression()
            if (expr is KtLambdaExpression && expr !in processedLambdas) {
                processedLambdas.add(expr)
                expr.bodyExpression?.let { body ->
                    extract(body, parent, depth)
                }
            }
        }
    }

    private fun isUiComposable(name: String): Boolean {
        return name.firstOrNull()?.isUpperCase() == true
    }

    private fun handleLazyListItems(
        expression: KtCallExpression,
        parent: ComposeUiNode,
        depth: Int
    ) {
        val name = expression.calleeExpression?.text ?: "items"

        val templateNode = ComposeUiNode("$name-template", depth = depth)

        // Ищем trailing lambda
        val trailingLambda = expression.lambdaArguments.lastOrNull()?.getLambdaExpression()

        if (trailingLambda != null) {
            trailingLambda.bodyExpression?.let { body ->
                extract(body, templateNode, depth + 1)
            }
        } else {
            // Ищем named lambda (последнюю)
            expression.valueArguments.reversed().forEach { arg ->
                val expr = arg.getArgumentExpression()
                if (expr is KtLambdaExpression) {
                    expr.bodyExpression?.let { body ->
                        extract(body, templateNode, depth + 1)
                        return@forEach
                    }
                }
            }
        }

        templateNode.children.forEach { child ->
            parent.children.add(child.deepCopy(child.depth))
        }
    }

    private fun extractParameters(call: KtCallExpression): Map<String, String> {
        val params = mutableMapOf<String, String>()

        call.valueArguments.forEach { arg ->
            val paramName = arg.getArgumentName()?.asName?.asString() ?: return@forEach
            val paramValue = arg.getArgumentExpression()?.text ?: ""
            params[paramName] = paramValue
        }

        return params
    }
}