package net.andrecarbajal.mclangtranslator.providers

import net.andrecarbajal.mclangtranslator.state.ApiKeyStore
import net.andrecarbajal.mclangtranslator.state.McTranslatorState

object ProviderFactory {
    val providerKeys = listOf("google", "deepl", "microsoft", "ai")

    fun displayName(key: String): String = when (key) {
        "google" -> "Google Translate"
        "deepl" -> "DeepL"
        "microsoft" -> "Microsoft Translator"
        "ai" -> "AI"
        else -> key
    }

    fun build(providerKey: String, state: McTranslatorState): TranslationProvider? {
        val apiKey = ApiKeyStore.get(providerKey)
        if (apiKey.isBlank()) return null
        return when (providerKey) {
            "google" -> GoogleTranslateProvider(apiKey)
            "deepl" -> DeepLProvider(apiKey, state.deeplApiHost.trimEnd('/'), state.deeplFormality)
            "microsoft" -> MicrosoftTranslatorProvider(apiKey, state.microsoftRegion)
            "ai" -> AiProvider(apiKey, state.aiEndpointUrl, state.aiModelName, state.aiSystemPromptOverride)
            else -> null
        }
    }
}

