package net.andrecarbajal.mclangtranslator.providers

import com.google.gson.JsonParser

class GoogleTranslateProvider(private val apiKey: String) : TranslationProvider {
    override val key = "google"
    override val displayName = "Google Translate"

    override fun getSupportedLanguages(): List<McLocale> {
        val url = "https://translation.googleapis.com/language/translate/v2/languages" +
                "?key=${HttpJsonClient.encode(apiKey)}&target=en"
        val root = JsonParser.parseString(HttpJsonClient.get(url)).asJsonObject
        return root["data"].asJsonObject["languages"].asJsonArray.mapNotNull { element ->
            val language = element.asJsonObject
            val providerCode = language["language"]?.asString ?: return@mapNotNull null
            val name = language["name"]?.asString ?: providerCode
            McLocale(toMcCode(providerCode), providerCode, name)
        }.distinctBy { it.mcCode }.sortedBy { it.displayName }
    }

    override fun translate(text: String, sourceLang: String, targetLang: String): String {
        val body = GsonProvider.gson.toJson(
            mapOf("q" to text, "source" to sourceLang, "target" to targetLang, "format" to "text"),
        )
        val root = JsonParser.parseString(
            HttpJsonClient.post(
                "https://translation.googleapis.com/language/translate/v2?key=${HttpJsonClient.encode(apiKey)}",
                body,
            ),
        ).asJsonObject
        return root["data"].asJsonObject["translations"].asJsonArray[0].asJsonObject["translatedText"].asString
    }

    override fun fallbackLanguages() =
        ProviderDefaults.commonLocales.map { it.copy(providerCode = googleCode(it.mcCode)) }

    private fun googleCode(mcCode: String) = when (mcCode) {
        "pt_br" -> "pt-BR"
        "pt_pt" -> "pt-PT"
        "zh_cn" -> "zh-CN"
        "zh_tw" -> "zh-TW"
        "es_mx" -> "es-419"
        else -> mcCode.substringBefore('_')
    }
}

fun toMcCode(providerCode: String): String {
    val normalized = providerCode.replace('-', '_').lowercase()
    return when (normalized) {
        "es" -> "es_es"
        "es_419", "es_mx" -> "es_mx"
        "es_es" -> "es_es"
        "es_ar" -> "es_ar"
        "es_uy" -> "es_uy"
        "es_ve" -> "es_ve"
        "pt" -> "pt_br"
        "de" -> "de_de"
        "fr" -> "fr_fr"
        "it" -> "it_it"
        "ru" -> "ru_ru"
        "zh", "zh_hans" -> "zh_cn"
        "zh_hant" -> "zh_tw"
        "ja" -> "ja_jp"
        "ko" -> "ko_kr"
        "pl" -> "pl_pl"
        "nl" -> "nl_nl"
        "tr" -> "tr_tr"
        "uk" -> "uk_ua"
        "cs" -> "cs_cz"
        "ar" -> "ar_sa"
        "sv" -> "sv_se"
        "no", "nb" -> "no_no"
        "fi" -> "fi_fi"
        "hu" -> "hu_hu"
        "ro" -> "ro_ro"
        "bg" -> "bg_bg"
        "hr" -> "hr_hr"
        "sk" -> "sk_sk"
        "da" -> "da_dk"
        else -> normalized
    }
}
