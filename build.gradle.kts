plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "ru.fav"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
//        local("/Applications/Android Studio.app")
        intellijIdeaUltimate("2024.2.4")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add plugin dependencies for compilation here:


        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("com.intellij.modules.xml")

    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Cognitive Load Analyzer"
        version = "1.0.0"
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "253.*"
        }
        description = "Cognitive Load Analyzer for Android UX/UI"
    }
    buildSearchableOptions = false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    runIde {

        systemProperty("idea.log.debug.categories", "#ru.fav.cognitiveloadanalyzer")
        args = listOf("/Users/alsu/AndroidStudioProjects/YogaDaily")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}