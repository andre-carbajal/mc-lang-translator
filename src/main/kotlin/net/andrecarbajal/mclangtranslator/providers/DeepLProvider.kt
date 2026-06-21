package net.andrecarbajal.mclangtranslator.providers

import com.google.gson.JsonParser

class DeepLProvider(
    private val apiKey: String,
    private val apiHost: String,
    private val formality: String,
) : TranslationProvider {
    override val key = "deepl"
    override val displayName = "DeepL"

    override fun getSupportedLanguages(): List<McLocale> {
        val body = HttpJsonClient.get(
            "$apiHost/v2/languages?type=target",
            headers = mapOf("Authorization" to "DeepL-Auth-Key $apiKey"),
        )
        return JsonParser.parseString(body).asJsonArray.mapNotNull { element ->
            val item = element.asJsonObject
            val providerCode = item["language"]?.asString ?: return@mapNotNull null
            val name = item["name"]?.asString ?: providerCode
            McLocale(toMcCode(providerCode), providerCode, name)
        }.distinctBy { it.mcCode }.sortedBy { it.displayName }
    }

    override fun translate(text: String, sourceLang: String, targetLang: String): String {
        val payload = mutableMapOf<String, Any>(
            "text" to listOf(text),
            "source_lang" to sourceLang.uppercase(),
            "target_lang" to targetLang.uppercase(),
        )
        if (formality != "default") payload["formality"] = formality
        val root = JsonParser.parseString(
            HttpJsonClient.post(
                "$apiHost/v2/translate",
                GsonProvider.gson.toJson(payload),
                headers = mapOf("Authorization" to "DeepL-Auth-Key $apiKey"),
            ),
        ).asJsonObject
        return root["translations"].asJsonArray[0].asJsonObject["text"].asString
    }

    override fun fallbackLanguages() = ProviderDefaults.commonLocales.mapNotNull {
        val code = when (it.mcCode) {
            "es_es", "es_mx" -> "ES"
            "pt_br" -> "PT-BR"
            "pt_pt" -> "PT-PT"
            "zh_cn", "zh_tw" -> "ZH"
            else -> it.providerCode.substringBefore('-').uppercase()
        }
        it.copy(providerCode = code)
    }
}
