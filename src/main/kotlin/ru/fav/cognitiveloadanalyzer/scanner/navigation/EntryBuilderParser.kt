package ru.fav.cognitiveloadanalyzer.scanner.navigation

import org.jetbrains.kotlin.psi.*
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationCall
import ru.fav.cognitiveloadanalyzer.core.model.navigation.NavigationTransition

/**
 * Парсит функции *EntryBuilder и извлекает переходы из entry<Route> блоков
 */
object EntryBuilderParser {

    /**
     * Находит все entry<Route> { ... } блоки и извлекает переходы
     */
    fun findTransitions(file: KtFile): List<NavigationTransition> {
        val transitions = mutableListOf<NavigationTransition>()

        // Ищем функции *EntryBuilder
        file.declarations.filterIsInstance<KtNamedFunction>()
            .filter { it.name?.endsWith("EntryBuilder") == true }
            .forEach { function ->
                extractFromEntryBuilder(function, transitions)
            }

        return transitions
    }

    private fun extractFromEntryBuilder(
        function: KtNamedFunction,
        transitions: MutableList<NavigationTransition>
    ) {
        function.bodyExpression?.let { body ->
            processExpression(body, transitions)
        }
    }

    private fun processExpression(
        expression: KtExpression,
        transitions: MutableList<NavigationTransition>
    ) {
        when (expression) {
            is KtBlockExpression -> {
                expression.statements.forEach { processExpression(it, transitions) }
            }

            is KtCallExpression -> {
                val callName = expression.calleeExpression?.text

                // Ищем entry<Route.*> { ... }
                if (callName == "entry") {
                    val routeType = extractEntryRoute(expression)
                    val screenName = extractScreenCall(expression)
                    val navCalls = extractNavigationCallsFromLambdas(expression)

                    if (routeType != null && screenName != null) {
                        // Связываем Route с экраном
                        navCalls.forEach { call ->
                            transitions.add(NavigationTransition(
                                from = routeType,  // Route.Auth
                                to = call.destination,  // AuthRoute.Login
                                type = call.type
                            ))
                        }
                    }
                }

                // Продолжаем поиск
                processLambdas(expression, transitions)
            }

            is KtLambdaExpression -> {
                expression.bodyExpression?.let { processExpression(it, transitions) }
            }
        }
    }

    /**
     * Извлекает тип Route
     */
    private fun extractEntryRoute(entryCall: KtCallExpression): String? {
        // entry<Route.Auth> - ищем тип в type arguments
        val typeArguments = entryCall.typeArguments
        if (typeArguments.isNotEmpty()) {
            val typeRef = typeArguments.first().typeReference
            return typeRef?.text  // "Route.Auth"
        }
        return null
    }

    /**
     * Извлекает имя экрана
     */
    private fun extractScreenCall(entryCall: KtCallExpression): String? {
        // entry<Route.Auth> { SplashScreen(...) }
        val lambda = entryCall.lambdaArguments.firstOrNull()?.getLambdaExpression()
        val body = lambda?.bodyExpression

        if (body is KtBlockExpression) {
            // Ищем первый вызов Composable
            val firstCall = body.statements.filterIsInstance<KtCallExpression>().firstOrNull()
            return firstCall?.calleeExpression?.text
        } else if (body is KtCallExpression) {
            return body.calleeExpression?.text
        }

        return null
    }

    /**
     * Извлекает вызовы навигации из лямбд-параметров
     */
    private fun extractNavigationCallsFromLambdas(entryCall: KtCallExpression): List<NavigationCall> {
        val calls = mutableListOf<NavigationCall>()

        // Ищем в trailing lambda: entry<...> { SplashScreen(...) }
        entryCall.lambdaArguments.forEach { lambdaArg ->
            lambdaArg.getLambdaExpression()?.bodyExpression?.let { body ->
                findNavigationCallsDeep(body, calls)
            }
        }

        return calls
    }

    /**
     * Глубокий поиск вызовов навигации во всех вложенных лямбдах
     */
    private fun findNavigationCallsDeep(
        expression: KtExpression,
        calls: MutableList<NavigationCall>
    ) {
        when (expression) {
            is KtBlockExpression -> {
                expression.statements.forEach { findNavigationCallsDeep(it, calls) }
            }

            is KtCallExpression -> {
                val callName = expression.calleeExpression?.text ?: ""

                // Проверяем, это вызов навигации?
                if (isNavigationCall(callName)) {
                    val destination = extractDestination(expression)
                    if (destination != null) {
                        calls.add(NavigationCall(
                            type = extractNavigationType(callName),
                            destination = destination
                        ))
                    }
                }

                // Ищем в лямбдах-параметрах (navigateToLogin = { ... })
                expression.valueArguments.forEach { arg ->
                    when (val argExpr = arg.getArgumentExpression()) {
                        is KtLambdaExpression -> {
                            argExpr.bodyExpression?.let { findNavigationCallsDeep(it, calls) }
                        }
                    }
                }

                // Ищем в trailing lambda
                expression.lambdaArguments.forEach { lambdaArg ->
                    lambdaArg.getLambdaExpression()?.bodyExpression?.let {
                        findNavigationCallsDeep(it, calls)
                    }
                }
            }

            is KtLambdaExpression -> {
                expression.bodyExpression?.let { findNavigationCallsDeep(it, calls) }
            }

            is KtDotQualifiedExpression -> {
                // navigator.navigateClearingStack(...)
                if (expression.selectorExpression is KtCallExpression) {
                    findNavigationCallsDeep(expression.selectorExpression!!, calls)
                }
            }
        }
    }

    private fun isNavigationCall(callName: String): Boolean {
        return callName.contains("navigate", ignoreCase = true) ||
               callName.contains("navigateTo", ignoreCase = true)
    }

    private fun extractNavigationType(callName: String): String {
        return when {
            callName.contains("navigateClearingStack") -> "navigateClearingStack"
            callName.contains("navigateBack") -> "navigateBack"
            else -> "navigate"
        }
    }

    private fun extractDestination(call: KtCallExpression): String? {
        return when (val firstArg = call.valueArguments.firstOrNull()?.getArgumentExpression()) {
            is KtDotQualifiedExpression -> {
                val text = firstArg.text

                // Убираем параметры
                if (text.contains('(')) {
                    text.substringBefore('(')
                } else {
                    text
                }
            }
            is KtCallExpression -> {
                firstArg.calleeExpression?.text
            }
            is KtNameReferenceExpression -> {
                firstArg.text
            }
            else -> null
        }
    }

    private fun processLambdas(
        call: KtCallExpression,
        transitions: MutableList<NavigationTransition>
    ) {
        call.lambdaArguments.forEach {
            it.getLambdaExpression()?.bodyExpression?.let { body ->
                processExpression(body, transitions)
            }
        }

        call.valueArguments.forEach {
            val expr = it.getArgumentExpression()
            if (expr is KtLambdaExpression) {
                expr.bodyExpression?.let { body ->
                    processExpression(body, transitions)
                }
            }
        }
    }
}