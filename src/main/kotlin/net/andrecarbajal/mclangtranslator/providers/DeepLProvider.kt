package net.andrecarbajal.mclangtranslator.providers

import com.deepl.api.DeepLClient
import com.deepl.api.DeepLClientOptions
import com.deepl.api.Formality
import com.deepl.api.TextTranslationOptions

class DeepLProvider(
    private val apiKey: String,
    private val apiHost: String,
    private val formality: String,
    private val clientAdapter: DeepLClientAdapter = SdkDeepLClientAdapter(apiKey, apiHost),
) : TranslationProvider {
    override val key = "deepl"
    override val displayName = "DeepL"

    override fun getSupportedLanguages(): List<McLocale> {
        return runCatching {
            clientAdapter.getTargetLanguages()
                .map { language -> McLocale(toMcCode(language.code), language.code, language.name) }
                .distinctBy { it.mcCode }
                .sortedBy { it.displayName }
        }.getOrElse { fallbackLanguages() }
    }

    override fun translate(text: String, sourceLang: String, targetLang: String): String {
        return try {
            clientAdapter.translateText(
                text = text,
                sourceLang = sourceLang.uppercase(),
                targetLang = targetLang.uppercase(),
                formality = formality.toDeepLFormality(),
            )
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw TranslationException("DeepL translation interrupted", e)
        } catch (e: Exception) {
            throw TranslationException(e.message ?: "DeepL translation failed", e)
        }
    }

    override fun fallbackLanguages() = ProviderDefaults.commonLocales.map {
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

data class DeepLLanguageInfo(val code: String, val name: String)

interface DeepLClientAdapter {
    @Throws(Exception::class)
    fun getTargetLanguages(): List<DeepLLanguageInfo>

    @Throws(Exception::class)
    fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String,
        formality: Formality?,
    ): String
}

private class SdkDeepLClientAdapter(apiKey: String, apiHost: String) : DeepLClientAdapter {
    private val client: DeepLClient

    init {
        val options = DeepLClientOptions()
        options.setServerUrl(apiHost.trimEnd('/'))
        options.setAppInfo("mc-lang-translator", "1.0.0")
        client = DeepLClient(apiKey, options)
    }

    override fun getTargetLanguages(): List<DeepLLanguageInfo> {
        return client.targetLanguages.map { language ->
            DeepLLanguageInfo(language.code, language.name)
        }
    }

    override fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String,
        formality: Formality?,
    ): String {
        val result = if (formality == null) {
            client.translateText(text, sourceLang, targetLang)
        } else {
            val options = TextTranslationOptions().setFormality(formality)
            client.translateText(text, sourceLang, targetLang, options)
        }
        return result.text
    }
}

private fun String.toDeepLFormality(): Formality? = when (this) {
    "less" -> Formality.Less
    "more" -> Formality.More
    "prefer_less" -> Formality.PreferLess
    "prefer_more" -> Formality.PreferMore
    else -> null
}
