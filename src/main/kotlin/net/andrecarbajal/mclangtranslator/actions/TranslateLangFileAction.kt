package net.andrecarbajal.mclangtranslator.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import net.andrecarbajal.mclangtranslator.engine.TranslationEngine
import net.andrecarbajal.mclangtranslator.engine.TranslationJob
import net.andrecarbajal.mclangtranslator.engine.sourceLocaleFromFileName
import net.andrecarbajal.mclangtranslator.providers.ProviderFactory
import net.andrecarbajal.mclangtranslator.state.McTranslatorStateService
import net.andrecarbajal.mclangtranslator.ui.LanguageSelectorDialog

class TranslateLangFileAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null &&
                file.extension.equals("json", ignoreCase = true) &&
                file.parent?.name == "lang"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val state = McTranslatorStateService.settings()
        val provider = ProviderFactory.build(state.lastUsedProvider, state)

        if (provider == null) {
            Messages.showErrorDialog(
                project,
                "No API key is configured for ${ProviderFactory.displayName(state.lastUsedProvider)}. Open Settings > Tools > MC Lang Translator.",
                "MC Lang Translator",
            )
            return
        }

        val dialog = LanguageSelectorDialog(project, file, provider, state)
        if (!dialog.showAndGet()) return

        val selectedLocales = dialog.getSelectedLocales()
        if (selectedLocales.isEmpty()) return

        state.lastUsedProvider = provider.key
        state.lastUsedLanguages = selectedLocales.map { it.mcCode }.toMutableList()

        val job = TranslationJob(
            sourceFile = file,
            targetLocales = selectedLocales,
            provider = provider,
            sourceLocale = sourceLocaleFromFileName(file.name),
            settings = state,
        )

        object : Task.Backgroundable(project, "Translating ${file.name}", true) {
            override fun run(indicator: ProgressIndicator) {
                val result = TranslationEngine().run(job, indicator) { locale, current, total ->
                    indicator.fraction = current.toDouble() / total
                    indicator.text2 = "[$locale] $current / $total keys"
                }
                ApplicationManager.getApplication().invokeLater {
                    val message = buildString {
                        append("Wrote ${result.writtenFiles.size} language file(s).")
                        if (result.warnings.isNotEmpty()) {
                            append(" ${result.warnings.size} warning(s).")
                        }
                    }
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("MC Lang Translator")
                        .createNotification(message, NotificationType.INFORMATION)
                        .notify(project)
                }
            }
        }.setCancelText("Stop translating").queue()
    }
}
