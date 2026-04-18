package ru.fav.cognitiveloadanalyzer.analyzer.screen

import ru.fav.cognitiveloadanalyzer.core.model.screen.ComposeUiNode

fun ComposeUiNode.isSelfSufficientComponent(): Boolean {
    val patterns = listOf(
        "Text",
        "Icon",
        "ListItem",
        "Image",
        "Button",
        "TextField",
        "Chip",
        "Dialog",
        "Slider",
        "Switch",
        "Checkbox",
        "RadioButton",
        "ProgressIndicator",
        "Snackbar",
        "Menu",
        "Tab",
        "Scaffold",
        "FloatingActionButton",
        "Badge",
        "Divider",
    )

    return patterns.any { pattern ->
        name.endsWith(pattern, ignoreCase = true)
    }
}

fun ComposeUiNode.isNonVisualElement(): Boolean {
    val nonVisual = listOf(
        "Spacer",
        "Box",
        "CompositionLocalProvider",
        "DisposableEffect",
        "LaunchedEffect",
        "SideEffect",
        "rememberCoroutineScope",
        "remember",
    )
    return nonVisual.any { name.endsWith(it, ignoreCase = true) }
}

fun ComposeUiNode.isClickable(): Boolean {
    val clickable = listOf(
        "Chip",
        "Switch",
        "Button",
        "Checkbox",
        "RadioButton",
        "Clickable",
        "ListItem",
        "TextField",
    )
    return clickable.any { name.endsWith(it, ignoreCase = true) }
}

fun ComposeUiNode.isAnimation(): Boolean {
    val animation = listOf(
        "Animated",
        "Transition",
        "Crossfade",
        "AnimatedVisibility",
        "AnimatedContent",
    )
    return animation.any { name.endsWith(it, ignoreCase = true) }
}

fun ComposeUiNode.needsSemantics(): Boolean {
    val semantics = listOf(
        "Icon",
        "Image",
        "TextField",
        "Button",
    )
    return semantics.any { name.endsWith(it, ignoreCase = true) }
}

fun ComposeUiNode.hasSemanticMetadata(): Boolean {
    // Для элементов, которым не нужна семантика, считаем что она есть
    if (!needsSemantics()) return true

    val hasContentDescription = name.contains("contentDescription", ignoreCase = true)
    val hasLabel = name.contains("label", ignoreCase = true)
    val hasPlaceholder = name.contains("placeholder", ignoreCase = true)
    val hasText = name.contains("text", ignoreCase = true)

    return when {
        // Icon/Image должны иметь contentDescription
        name.endsWith("Icon", ignoreCase = true) ||
                name.endsWith("Image", ignoreCase = true) -> hasContentDescription

        // TextField должен иметь label или placeholder
        name.endsWith("TextField", ignoreCase = true) -> hasLabel || hasPlaceholder

        // Button должен иметь text (contentDescription опционально)
        name.endsWith("Button", ignoreCase = true) -> hasText || hasContentDescription

        else -> true
    }
}

/**
 * Проверяет, является ли элемент текстовым (содержит текстовую информацию)
 */
fun ComposeUiNode.isTextElement(): Boolean {
    val textElements = listOf(
        "Text",
        "TextField",
        "OutlinedTextField",
        "BasicTextField",
    )
    return textElements.any { name.endsWith(it, ignoreCase = true) }
}