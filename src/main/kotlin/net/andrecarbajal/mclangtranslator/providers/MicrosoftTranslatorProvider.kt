package net.andrecarbajal.mclangtranslator.providers

import com.google.gson.JsonParser

class MicrosoftTranslatorProvider(
    private val apiKey: String,
    private val region: String,
) : TranslationProvider {
    override val key = "microsoft"
    override val displayName = "Microsoft Translator"

    override fun getSupportedLanguages(): List<McLocale> {
        val root = JsonParser.parseString(
            HttpJsonClient.get("https://api.cognitive.microsofttranslator.com/languages?api-version=3.0&scope=translation"),
        ).asJsonObject
        val translations = root["translation"].asJsonObject
        return translations.entrySet().map { (providerCode, details) ->
            val name = details.asJsonObject["name"]?.asString ?: providerCode
            McLocale(toMcCode(providerCode), providerCode, name)
        }.distinctBy { it.mcCode }.sortedBy { it.displayName }
    }

    override fun translate(text: String, sourceLang: String, targetLang: String): String {
        val url = "https://api.cognitive.microsofttranslator.com/translate" +
                "?api-version=3.0&from=${HttpJsonClient.encode(sourceLang)}&to=${HttpJsonClient.encode(targetLang)}"
        val headers = mutableMapOf(
            "Ocp-Apim-Subscription-Key" to apiKey,
        )
        if (region.isNotBlank()) headers["Ocp-Apim-Subscription-Region"] = region
        val root = JsonParser.parseString(
            HttpJsonClient.post(url, GsonProvider.gson.toJson(listOf(mapOf("Text" to text))), headers = headers),
        ).asJsonArray
        return root[0].asJsonObject["translations"].asJsonArray[0].asJsonObject["text"].asString
    }

    override fun fallbackLanguages() = ProviderDefaults.commonLocales.map {
        val code = when (it.mcCode) {
            "pt_br" -> "pt-br"
            "pt_pt" -> "pt-pt"
            "zh_cn" -> "zh-Hans"
            "zh_tw" -> "zh-Hant"
            else -> it.providerCode.substringBefore('-')
        }
        it.copy(providerCode = code)
    }
}
