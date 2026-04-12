package ru.fav.cognitiveloadanalyzer.scanner

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Глобальный реестр всех Composable функций в проекте
 */
class ComposableRegistry {
    // Хранение по FQN (полному имени)
    private val functionsByFqn = mutableMapOf<String, KtNamedFunction>()
    
    // Дополнительный индекс по простому имени для быстрого поиска
    private val functionsBySimpleName = mutableMapOf<String, MutableList<KtNamedFunction>>()
    
    fun register(function: KtNamedFunction) {
        val simpleName = function.name ?: return
        val fqn = function.fqName?.asString() ?: return
        
        // Регистрируем по FQN
        functionsByFqn[fqn] = function
        
        // Индексируем по простому имени (может быть несколько)
        functionsBySimpleName.getOrPut(simpleName) { mutableListOf() }.add(function)
    }
    
    /**
     * Поиск по FQN (точное совпадение)
     */
    fun findByFqn(fqn: String): KtNamedFunction? = functionsByFqn[fqn]
    
    /**
     * Поиск по простому имени (может вернуть несколько вариантов)
     */
    fun findBySimpleName(name: String): List<KtNamedFunction> = 
        functionsBySimpleName[name] ?: emptyList()
    
    /**
     * Умный поиск с учетом импортов файла
     */
    fun resolve(name: String, contextFile: KtFile): KtNamedFunction? {
        // Попытка найти по FQN (если передано полное имя)
        functionsByFqn[name]?.let { return it }
        
        // Поиск по простому имени
        val candidates = findBySimpleName(name)
        
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()
        
        // Если несколько вариантов - резолвим через импорты
        return resolveByImports(name, candidates, contextFile)
    }
    
    /**
     * Резолвинг через импорты контекстного файла
     */
    private fun resolveByImports(
        simpleName: String,
        candidates: List<KtNamedFunction>,
        contextFile: KtFile
    ): KtNamedFunction? {
        val imports = contextFile.importDirectives
        
        // Проверяем явные импорты
        for (import in imports) {
            val importPath = import.importPath?.pathStr ?: continue
            
            // import com.example.MyButton
            if (importPath.endsWith(".$simpleName")) {
                return candidates.firstOrNull { 
                    it.fqName?.asString() == importPath 
                }
            }
            
            // import com.example.*
            if (importPath.endsWith(".*")) {
                val packagePath = importPath.removeSuffix(".*")
                candidates.firstOrNull { 
                    it.fqName?.asString()?.startsWith("$packagePath.") == true
                }?.let { return it }
            }
        }
        
        // Если не нашли через импорты - проверяем тот же пакет
        val contextPackage = contextFile.packageFqName.asString()
        candidates.firstOrNull {
            it.containingKtFile.packageFqName.asString() == contextPackage
        }?.let { return it }
        
        // По умолчанию берём первый вариант
        return candidates.firstOrNull()
    }
    
    fun getAll(): List<KtNamedFunction> = functionsByFqn.values.toList()
    
    fun clear() {
        functionsByFqn.clear()
        functionsBySimpleName.clear()
    }
    
    fun size() = functionsByFqn.size
}