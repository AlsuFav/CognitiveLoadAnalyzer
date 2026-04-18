package ru.fav.cognitiveloadanalyzer.scanner.navigation

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import ru.fav.cognitiveloadanalyzer.core.model.navigation.RouteDefinition

/**
 * Парсит Route классы (sealed class с дестинациями)
 */
object RouteParser {

    fun findRoutes(file: KtFile): List<RouteDefinition> {
        val routes = mutableListOf<RouteDefinition>()

        file.declarations.forEach { declaration ->
            when (declaration) {
                is KtClass -> {
                    // Проверяем, что это sealed class И это Route
                    if (declaration.isSealed() && isRouteClass(declaration)) {
                        routes.addAll(extractRouteFromSealedClass(declaration))
                    }
                }
            }
        }

        return routes
    }

    /**
     * Проверяет, является ли класс Route классом
     */
    private fun isRouteClass(ktClass: KtClass): Boolean {
        val className = ktClass.name ?: return false

        // Проверяем имя класса (Route, AuthRoute, и т.д.)
        if (className.endsWith("Route", ignoreCase = true)) return true

        // Проверяем наследование от NavKey
        val superTypes = ktClass.getSuperTypeList()?.entries
        val inheritsFromNavKey = superTypes?.any {
            it.typeAsUserType?.referencedName == "NavKey"
        } ?: false

        return inheritsFromNavKey
    }

    private fun extractRouteFromSealedClass(sealedClass: KtClass): List<RouteDefinition> {
        val routes = mutableListOf<RouteDefinition>()
        val sealedClassName = sealedClass.name ?: return routes

        sealedClass.body?.declarations?.forEach { declaration ->
            when (declaration) {
                is KtObjectDeclaration -> {
                    val routeName = declaration.name ?: return@forEach

                    if (routeName == "Companion") return@forEach

                    routes.add(RouteDefinition(
                        fullName = "$sealedClassName.$routeName",
                        simpleName = routeName,
                        hasParameters = false
                    ))
                }

                is KtClass -> {
                    val routeName = declaration.name ?: return@forEach
                    val hasParams = declaration.primaryConstructorParameters.isNotEmpty()
                    routes.add(RouteDefinition(
                        fullName = "$sealedClassName.$routeName",
                        simpleName = routeName,
                        hasParameters = hasParams
                    ))
                }
            }
        }

        return routes
    }
}