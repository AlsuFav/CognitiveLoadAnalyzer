package ru.fav.cognitiveloadanalyzer.scanner.navigation

import org.jetbrains.kotlin.psi.*
import ru.fav.cognitiveloadanalyzer.core.model.navigation.EntryPointType
import ru.fav.cognitiveloadanalyzer.core.model.navigation.RouteEntryPoint

object EntryPointParser {

    fun findEntryPoints(files: List<KtFile>): List<RouteEntryPoint> {
        val result = mutableListOf<RouteEntryPoint>()

        val bottomNavRegistry = BottomNavItemParser.parse(files)

        files.forEach { file ->
            result.addAll(findInFile(file, bottomNavRegistry))
        }

        return result.distinctBy { it.route to it.type }
    }

    private fun findInFile(
        file: KtFile,
        bottomNavRegistry: Map<String, BottomNavItemParser.BottomNavEntry>
    ): List<RouteEntryPoint> {
        val result = mutableListOf<RouteEntryPoint>()

        file.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)
                val name = expression.calleeExpression?.text ?: return

                when {
                    isBackStackCall(name) -> {
                        extractBackStackStart(expression)?.let { result.add(it) }
                    }
                    isBottomNavCall(name) -> {
                        result.addAll(
                            extractBottomNavRoutes(expression, bottomNavRegistry)
                        )
                    }
                    name == "NavHost" -> {
                        extractStartDestination(expression)?.let { result.add(it) }
                    }
                    name == "deepLink" -> {
                        extractDeepLink(expression)?.let { result.add(it) }
                    }
                    isDrawerCall(name) -> {
                        result.addAll(extractDrawerRoutes(expression))
                    }
                    isTopBarCall(name) -> {
                        result.addAll(extractTopBarRoutes(expression))
                    }
                }
            }
        })

        return result
    }

    private fun isBottomNavCall(name: String) =
        name in setOf("BottomNavigation", "NavigationBar", "BottomNavigationView")

    private fun extractBottomNavRoutes(
        call: KtCallExpression,
        registry: Map<String, BottomNavItemParser.BottomNavEntry>
    ): List<RouteEntryPoint> {
        val result = mutableListOf<RouteEntryPoint>()

        // Ищем forEach внутри NavigationBar
        call.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)

                if (expression.calleeExpression?.text == "forEach") {
                    // Внутри forEach ищем NavigationBarItem
                    val hasNavBarItem = containsNavBarItem(expression)
                    val usesRouteField = containsRouteFieldAccess(expression)

                    if (hasNavBarItem && usesRouteField && registry.isNotEmpty()) {
                        // Паттерн: forEach { item -> NavigationBarItem(onClick = { navigate(item.route) }) }
                        // Берём все маршруты из registry
                        registry.values.forEach { entry ->
                            result.add(
                                RouteEntryPoint(
                                    route = entry.route,
                                    type  = EntryPointType.BOTTOM_NAVIGATION,
                                    label = entry.label
                                )
                            )
                        }
                    }
                }
            }
        })

        // Если forEach не нашёл - пробуем прямые маршруты
        if (result.isEmpty()) {
            call.accept(object : KtTreeVisitorVoid() {
                override fun visitCallExpression(expression: KtCallExpression) {
                    super.visitCallExpression(expression)
                    val name = expression.calleeExpression?.text ?: return
                    if (name in setOf("BottomNavigationItem", "NavigationBarItem")) {
                        val route = extractDirectRoute(expression)
                        if (route != null && !looksLikeVariable(route)) {
                            result.add(
                                RouteEntryPoint(
                                    route = route,
                                    type  = EntryPointType.BOTTOM_NAVIGATION,
                                    label = null
                                )
                            )
                        }
                    }
                }
            })
        }

        return result
    }

    // Проверяем что внутри есть NavigationBarItem
    private fun containsNavBarItem(expr: KtExpression): Boolean {
        var found = false
        expr.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(call: KtCallExpression) {
                super.visitCallExpression(call)
                if (call.calleeExpression?.text in setOf(
                        "NavigationBarItem", "BottomNavigationItem"
                    )
                ) found = true
            }
        })
        return found
    }

    // Проверяем что есть обращение к .route у переменной (item.route, bottomNavItem.route)
    private fun containsRouteFieldAccess(expr: KtExpression): Boolean {
        var found = false
        expr.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(dotExpr: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(dotExpr)
                if (dotExpr.selectorExpression?.text == "route") {
                    // receiver — это переменная (не Route.X)
                    val receiver = dotExpr.receiverExpression
                    if (receiver is KtNameReferenceExpression) {
                        found = true
                    }
                }
            }
        })
        return found
    }

    private fun looksLikeVariable(route: String): Boolean {
        return route.endsWith(".route") &&
                !route.startsWith("Route") &&
                route.first().isLowerCase()
    }

    private fun extractDirectRoute(item: KtCallExpression): String? {
        item.valueArguments.forEach { arg ->
            val expr = arg.getArgumentExpression()
            if (expr is KtBinaryExpression) {
                listOfNotNull(expr.left, expr.right).forEach { side ->
                    val route = tryExtractRoute(side)
                    if (route != null) return route
                }
            }
        }

        item.valueArguments.forEach { arg ->
            if (arg.getArgumentName()?.text == "onClick") {
                return findNavigateCallRoute(arg.getArgumentExpression())
            }
        }
        item.lambdaArguments.forEach { lambda ->
            return findNavigateCallRoute(lambda.getLambdaExpression())
        }

        return null
    }

    private fun tryExtractRoute(expr: KtExpression?): String? = when (expr) {
        is KtDotQualifiedExpression  -> expr.text
        is KtNameReferenceExpression -> expr.text
        else -> null
    }

    private fun findNavigateCallRoute(expr: KtExpression?): String? {
        if (expr == null) return null
        var result: String? = null
        expr.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(call: KtCallExpression) {
                super.visitCallExpression(call)
                val n = call.calleeExpression?.text ?: return
                if (n.contains("navigate", ignoreCase = true)) {
                    val arg = call.valueArguments.firstOrNull()
                        ?.getArgumentExpression()
                    result = tryExtractRoute(arg)
                }
            }
        })
        return result
    }

    private fun extractStartDestination(navHost: KtCallExpression): RouteEntryPoint? {
        navHost.valueArguments.forEach { arg ->
            if (arg.getArgumentName()?.text == "startDestination") {
                val expr = arg.getArgumentExpression()
                val route = when (expr) {
                    is KtDotQualifiedExpression  -> expr.text
                    is KtNameReferenceExpression -> expr.text
                    is KtStringTemplateExpression -> expr.text.trim('"')
                    else -> null
                } ?: return null
                return RouteEntryPoint(
                    route = route,
                    type  = EntryPointType.START_DESTINATION,
                    label = "Start"
                )
            }
        }
        return null
    }

    private fun extractDeepLink(call: KtCallExpression): RouteEntryPoint? {
        val uriPattern = call.valueArguments
            .firstOrNull { it.getArgumentName()?.text == "uriPattern" }
            ?.getArgumentExpression()?.text?.trim('"') ?: return null
        val parentRoute = findParentEntryRoute(call)
        return RouteEntryPoint(
            route = parentRoute ?: uriPattern,
            type  = EntryPointType.DEEP_LINK,
            label = uriPattern
        )
    }

    private fun findParentEntryRoute(element: KtElement): String? {
        var parent = element.parent
        while (parent != null) {
            if (parent is KtCallExpression &&
                parent.calleeExpression?.text == "entry"
            ) {
                return parent.typeArguments.firstOrNull()?.typeReference?.text
            }
            parent = parent.parent
        }
        return null
    }

    private fun isDrawerCall(name: String) =
        name in setOf(
            "NavigationDrawer", "ModalNavigationDrawer",
            "PermanentNavigationDrawer", "NavigationDrawerItem"
        )

    private fun extractDrawerRoutes(call: KtCallExpression): List<RouteEntryPoint> {
        val result = mutableListOf<RouteEntryPoint>()
        call.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)
                if (expression.calleeExpression?.text == "NavigationDrawerItem") {
                    val route = extractDirectRoute(expression)
                    if (route != null) {
                        result.add(RouteEntryPoint(route, EntryPointType.DRAWER))
                    }
                }
            }
        })
        return result
    }

    private fun isTopBarCall(name: String) =
        name in setOf(
            "TopAppBar", "CenterAlignedTopAppBar",
            "MediumTopAppBar", "LargeTopAppBar"
        )

    private fun extractTopBarRoutes(call: KtCallExpression): List<RouteEntryPoint> {
        val result = mutableListOf<RouteEntryPoint>()
        call.valueArguments.forEach { arg ->
            if (arg.getArgumentName()?.text == "navigationIcon") {
                val route = findNavigateCallRoute(arg.getArgumentExpression())
                if (route != null) {
                    result.add(RouteEntryPoint(route, EntryPointType.TOP_BAR, "TopBar"))
                }
            }
        }
        return result
    }

    private fun isBackStackCall(name: String) =
        name in setOf(
            "rememberNavBackStack",
            "NavBackStack"
        )

    private fun extractBackStackStart(
        call: KtCallExpression
    ): RouteEntryPoint? {
        val arg = call.valueArguments.firstOrNull()
            ?.getArgumentExpression()
            ?: return null

        val route = when (arg) {
            is KtDotQualifiedExpression -> arg.text
            is KtNameReferenceExpression -> arg.text
            is KtStringTemplateExpression -> arg.text.trim('"')
            else -> null
        } ?: return null

        return RouteEntryPoint(
            route = route,
            type = EntryPointType.START_DESTINATION,
            label = "Start"
        )
    }
}