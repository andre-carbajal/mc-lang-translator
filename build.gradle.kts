import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.gradle.api.Project

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    implementation("com.deepl.api:deepl-java:1.16.0")
    implementation("com.google.code.gson:gson:2.14.0")

    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2025.2.6.2")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        description = provider {
            project.readmeAsPluginDescription()
        }
    }
}

fun Project.readmeAsPluginDescription(): String {
    val lines = layout.projectDirectory.file("README.md").asFile.readLines()
    val html = StringBuilder()
    var inList = false
    var inCodeBlock = false

    fun closeList() {
        if (inList) {
            html.append("</ul>\n")
            inList = false
        }
    }

    lines.forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("```") -> {
                closeList()
                inCodeBlock = !inCodeBlock
            }

            inCodeBlock -> Unit

            trimmed.startsWith("# ") -> {
                closeList()
                html.append("<h1>").append(trimmed.removePrefix("# ").escapeHtml()).append("</h1>\n")
            }

            trimmed.startsWith("## ") -> {
                closeList()
                html.append("<h2>").append(trimmed.removePrefix("## ").escapeHtml()).append("</h2>\n")
            }

            trimmed.startsWith("- ") -> {
                if (!inList) {
                    html.append("<ul>\n")
                    inList = true
                }
                html.append("<li>").append(trimmed.removePrefix("- ").escapeHtml()).append("</li>\n")
            }

            trimmed.isBlank() -> closeList()

            else -> {
                closeList()
                html.append("<p>").append(trimmed.escapeHtml()).append("</p>\n")
            }
        }
    }
    closeList()
    return html.toString().trim()
}

fun String.escapeHtml(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
