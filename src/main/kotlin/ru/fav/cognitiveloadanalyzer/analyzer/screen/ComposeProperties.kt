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