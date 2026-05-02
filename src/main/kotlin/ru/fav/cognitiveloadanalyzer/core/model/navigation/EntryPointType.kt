package ru.fav.cognitiveloadanalyzer.core.model.navigation

enum class EntryPointType {
    BOTTOM_NAVIGATION,    // BottomNavigation / NavigationBar item
    TOP_BAR,              // TopAppBar с навигацией
    DRAWER,               // NavigationDrawer
    DEEP_LINK,            // deepLink { ... }
    PROGRAMMATIC,         // navigate() из кода
    START_DESTINATION,    // startDestination
    UNKNOWN
}