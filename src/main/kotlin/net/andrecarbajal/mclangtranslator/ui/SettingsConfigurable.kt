package net.andrecarbajal.mclangtranslator.ui

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import net.andrecarbajal.mclangtranslator.providers.ProviderFactory
import net.andrecarbajal.mclangtranslator.state.ApiKeyStore
import net.andrecarbajal.mclangtranslator.state.McTranslatorState
import net.andrecarbajal.mclangtranslator.state.McTranslatorStateService
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class SettingsConfigurable : Configurable {
    private val activeProvider = ComboBox(ProviderFactory.providerKeys.toTypedArray())
    private val googleKey = JPasswordField(32)
    private val deeplKey = JPasswordField(32)
    private val microsoftKey = JPasswordField(32)
    private val aiKey = JPasswordField(32)
    private val microsoftRegion = JTextField(24)
    private val deeplApiHost = JTextField(32)
    private val deeplFormality = ComboBox(arrayOf("default", "less", "more", "prefer_less", "prefer_more"))
    private val aiEndpointUrl = JTextField(32)
    private val aiModelName = JTextField(24)
    private val aiSystemPrompt = JTextArea(4, 32)
    private val requestDelayMs = JTextField(8)
    private val skipExistingKeys = JCheckBox("Skip keys that already exist in the target file")
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "MC Lang Translator"

    override fun createComponent(): JComponent {
        if (panel == null) {
            panel = JPanel(BorderLayout()).apply {
                add(buildForm(), BorderLayout.NORTH)
            }
        }
        reset()
        return panel!!
    }

    private fun buildForm(): JPanel {
        val form = JPanel(GridBagLayout())
        var row = 0
        fun addRow(label: String, component: JComponent) {
            form.add(JLabel(label), GridBagConstraints().apply {
                gridx = 0
                gridy = row
                anchor = GridBagConstraints.WEST
                insets = Insets(4, 4, 4, 8)
            })
            form.add(component, GridBagConstraints().apply {
                gridx = 1
                gridy = row++
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(4, 4, 4, 4)
            })
        }

        addRow("Active provider:", activeProvider)
        addRow("Google API key:", googleKey)
        addRow("DeepL API key:", deeplKey)
        addRow("DeepL API host:", deeplApiHost)
        addRow("DeepL formality:", deeplFormality)
        addRow("Microsoft API key:", microsoftKey)
        addRow("Microsoft region:", microsoftRegion)
        addRow("AI API key:", aiKey)
        addRow("AI endpoint URL:", aiEndpointUrl)
        addRow("AI model:", aiModelName)
        addRow("AI system prompt:", aiSystemPrompt)
        addRow("Delay between requests (ms):", requestDelayMs)
        form.add(skipExistingKeys, GridBagConstraints().apply {
            gridx = 1
            gridy = row++
            anchor = GridBagConstraints.WEST
            insets = Insets(4, 4, 4, 4)
        })
        form.add(JButton("Test connection").apply {
            addActionListener { testConnection() }
        }, GridBagConstraints().apply {
            gridx = 1
            gridy = row
            anchor = GridBagConstraints.WEST
            insets = Insets(8, 4, 4, 4)
        })
        return form
    }

    override fun isModified(): Boolean {
        val state = McTranslatorStateService.settings()
        return activeProvider.selectedItem != state.lastUsedProvider ||
                String(googleKey.password) != ApiKeyStore.get("google") ||
                String(deeplKey.password) != ApiKeyStore.get("deepl") ||
                String(microsoftKey.password) != ApiKeyStore.get("microsoft") ||
                String(aiKey.password) != ApiKeyStore.get("ai") ||
                microsoftRegion.text != state.microsoftRegion ||
                deeplApiHost.text != state.deeplApiHost ||
                deeplFormality.selectedItem != state.deeplFormality ||
                aiEndpointUrl.text != state.aiEndpointUrl ||
                aiModelName.text != state.aiModelName ||
                aiSystemPrompt.text != state.aiSystemPromptOverride ||
                requestDelayMs.text.toLongOrNull() != state.requestDelayMs ||
                skipExistingKeys.isSelected != state.skipExistingKeys
    }

    override fun apply() {
        val state = McTranslatorStateService.settings()
        state.lastUsedProvider = activeProvider.selectedItem as String
        state.microsoftRegion = microsoftRegion.text.trim().ifBlank { "global" }
        state.deeplApiHost = deeplApiHost.text.trim().ifBlank { "https://api-free.deepl.com" }
        state.deeplFormality = deeplFormality.selectedItem as String
        state.aiEndpointUrl = aiEndpointUrl.text.trim().ifBlank { "https://api.openai.com/v1/chat/completions" }
        state.aiModelName = aiModelName.text.trim().ifBlank { "gpt-4o" }
        state.aiSystemPromptOverride = aiSystemPrompt.text
        state.requestDelayMs = requestDelayMs.text.toLongOrNull()?.coerceIn(0L, 5000L) ?: 50L
        state.skipExistingKeys = skipExistingKeys.isSelected
        ApiKeyStore.set("google", String(googleKey.password))
        ApiKeyStore.set("deepl", String(deeplKey.password))
        ApiKeyStore.set("microsoft", String(microsoftKey.password))
        ApiKeyStore.set("ai", String(aiKey.password))
    }

    override fun reset() {
        val state = McTranslatorStateService.settings()
        activeProvider.selectedItem = state.lastUsedProvider
        googleKey.text = ApiKeyStore.get("google")
        deeplKey.text = ApiKeyStore.get("deepl")
        microsoftKey.text = ApiKeyStore.get("microsoft")
        aiKey.text = ApiKeyStore.get("ai")
        microsoftRegion.text = state.microsoftRegion
        deeplApiHost.text = state.deeplApiHost
        deeplFormality.selectedItem = state.deeplFormality
        aiEndpointUrl.text = state.aiEndpointUrl
        aiModelName.text = state.aiModelName
        aiSystemPrompt.text = state.aiSystemPromptOverride
        requestDelayMs.text = state.requestDelayMs.toString()
        skipExistingKeys.isSelected = state.skipExistingKeys
    }

    private fun testConnection() {
        val state = snapshot()
        val provider = ProviderFactory.build(state.lastUsedProvider, state)
        if (provider == null) {
            Messages.showErrorDialog(
                "No API key is configured for ${ProviderFactory.displayName(state.lastUsedProvider)}.",
                displayName
            )
            return
        }
        try {
            val count = provider.getSupportedLanguages().size
            Messages.showInfoMessage("Connection OK. $count languages available.", displayName)
        } catch (e: Exception) {
            Messages.showErrorDialog(e.message ?: "Connection failed.", displayName)
        }
    }

    private fun snapshot(): McTranslatorState {
        apply()
        return McTranslatorStateService.settings()
    }
}
