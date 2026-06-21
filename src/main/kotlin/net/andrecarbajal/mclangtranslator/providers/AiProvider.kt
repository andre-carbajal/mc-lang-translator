package net.andrecarbajal.mclangtranslator.providers

import com.google.gson.JsonParser

class AiProvider(
    private val apiKey: String,
    private val endpointUrl: String,
    private val modelName: String,
    private val systemPromptOverride: String,
) : TranslationProvider {
    override val key = "ai"
    override val displayName = "AI ($modelName)"

    override fun getSupportedLanguages() = fallbackLanguages()

    override fun translate(text: String, sourceLang: String, targetLang: String): String {
        val prompt = systemPromptOverride.ifBlank {
            """
            You are a professional translator for Minecraft mod localizations.
            Translate from $sourceLang to $targetLang.
            Return only the translated text.
            Preserve placeholder tokens exactly, including tokens like __MC_PLACEHOLDER_0__.
            Preserve formatting characters like \n and \t.
            """.trimIndent()
        }
        val body = GsonProvider.gson.toJson(
            mapOf(
                "model" to modelName,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to prompt),
                    mapOf("role" to "user", "content" to text),
                ),
                "temperature" to 0.2,
            ),
        )
        val root = JsonParser.parseString(
            HttpJsonClient.post(endpointUrl, body, headers = mapOf("Authorization" to "Bearer $apiKey")),
        ).asJsonObject
        return root["choices"].asJsonArray[0].asJsonObject["message"].asJsonObject["content"].asString.trim()
    }

    override fun fallbackLanguages() = ProviderDefaults.commonLocales
}
