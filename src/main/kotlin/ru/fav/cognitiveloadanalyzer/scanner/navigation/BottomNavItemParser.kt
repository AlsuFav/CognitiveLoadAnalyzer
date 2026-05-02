package ru.fav.cognitiveloadanalyzer.scanner.navigation

import org.jetbrains.kotlin.psi.*

object BottomNavItemParser {

    data class BottomNavEntry(
        val objectName: String,  // "Categories", "Favourites"
        val route: String,       // "Route.Categories"
        val label: String?       // из titleResId или label параметра
    )

    /**
     * Ищет sealed class у которых:
     * - есть параметр route: Route (или похожий тип)
     * - объекты внутри задают route = Route.X
     *
     * Покрывает паттерн BottomNavItem
     */
    fun parse(files: List<KtFile>): Map<String, BottomNavEntry> {
        val result = mutableMapOf<String, BottomNavEntry>()

        files.forEach { file ->
            file.declarations.filterIsInstance<KtClass>().forEach { ktClass ->
                if (ktClass.isSealed() && looksLikeBottomNavClass(ktClass)) {
                    val entries = extractEntries(ktClass)
                    entries.forEach { entry ->
                        result[entry.route] = entry
                    }
                }
            }
        }

        return result
    }

    // Эвристика: sealed class с параметром "route" в конструкторе
    private fun looksLikeBottomNavClass(ktClass: KtClass): Boolean {
        val constructorParams = ktClass.primaryConstructorParameters
        val hasRouteParam = constructorParams.any {
            it.name?.contains("route", ignoreCase = true) == true
        }
        // Или имя класса содержит BottomNav / Tab / NavItem
        val nameMatches = ktClass.name?.let { name ->
            name.contains("BottomNav", ignoreCase = true) ||
            name.contains("NavItem",   ignoreCase = true) ||
            name.contains("TabItem",   ignoreCase = true) ||
            name.contains("NavTab",    ignoreCase = true)
        } ?: false

        return hasRouteParam || nameMatches
    }

    private fun extractEntries(sealedClass: KtClass): List<BottomNavEntry> {
        val result = mutableListOf<BottomNavEntry>()

        sealedClass.body?.declarations?.forEach { decl ->
            when (decl) {
                // data object Categories : BottomNavItem(...)
                is KtObjectDeclaration -> {
                    if (decl.name == "Companion") return@forEach
                    val entry = extractFromObject(decl) ?: return@forEach
                    result.add(entry)
                }
                // Вложенные классы если есть
                is KtClass -> {
                    val entry = extractFromClass(decl) ?: return@forEach
                    result.add(entry)
                }
            }
        }

        return result
    }

    // data object Categories : BottomNavItem(route = Route.Categories, ...)
    private fun extractFromObject(obj: KtObjectDeclaration): BottomNavEntry? {
        val objectName = obj.name ?: return null

        // Ищем вызов родительского конструктора
        val superCall = obj.superTypeListEntries
            .filterIsInstance<KtSuperTypeCallEntry>()
            .firstOrNull() ?: return null

        val route = extractRouteArg(superCall.valueArgumentList?.arguments ?: emptyList())
            ?: return null

        val label = extractLabelArg(superCall.valueArgumentList?.arguments ?: emptyList())

        return BottomNavEntry(
            objectName = objectName,
            route      = route,
            label      = label
        )
    }

    private fun extractFromClass(ktClass: KtClass): BottomNavEntry? {
        val className = ktClass.name ?: return null
        val superCall = ktClass.superTypeListEntries
            .filterIsInstance<KtSuperTypeCallEntry>()
            .firstOrNull() ?: return null

        val route = extractRouteArg(superCall.valueArgumentList?.arguments ?: emptyList())
            ?: return null
        val label = extractLabelArg(superCall.valueArgumentList?.arguments ?: emptyList())

        return BottomNavEntry(objectName = className, route = route, label = label)
    }

    // Извлекаем route = Route.Categories из аргументов конструктора
    private fun extractRouteArg(args: List<KtValueArgument>): String? {
        // Ищем именованный аргумент route = ...
        args.forEach { arg ->
            if (arg.getArgumentName()?.text == "route") {
                return extractRouteText(arg.getArgumentExpression())
            }
        }
        // Или первый позиционный аргумент
        return extractRouteText(args.firstOrNull()?.getArgumentExpression())
    }

    private fun extractRouteText(expr: KtExpression?): String? = when (expr) {
        is KtDotQualifiedExpression  -> expr.text   // Route.Categories
        is KtNameReferenceExpression -> expr.text   // Categories (если импортировано)
        is KtCallExpression          -> expr.calleeExpression?.text  // Route.Categories()
        else -> null
    }

    // Извлекаем label из titleResId или label аргумента
    private fun extractLabelArg(args: List<KtValueArgument>): String? {
        args.forEach { arg ->
            val name = arg.getArgumentName()?.text ?: return@forEach
            // titleResId = R.string.poses_tab_title → берём последний сегмент
            if (name.contains("title", ignoreCase = true) ||
                name.contains("label", ignoreCase = true)
            ) {
                val expr = arg.getArgumentExpression()
                return when (expr) {
                    is KtDotQualifiedExpression ->
                        // R.string.poses_tab_title → "poses_tab_title"
                        expr.text.substringAfterLast('.')
                    is KtStringTemplateExpression ->
                        expr.text.trim('"')
                    else -> null
                }
            }
        }
        return null
    }
}