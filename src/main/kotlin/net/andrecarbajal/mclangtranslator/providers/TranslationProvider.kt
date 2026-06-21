package net.andrecarbajal.mclangtranslator.providers

data class McLocale(
    var mcCode: String = "",
    var providerCode: String = "",
    var displayName: String = "",
)

interface TranslationProvider {
    val key: String
    val displayName: String

    fun getSupportedLanguages(): List<McLocale>

    fun translate(text: String, sourceLang: String, targetLang: String): String

    fun fallbackLanguages(): List<McLocale>

    fun providerCodeForMcCode(mcCode: String): String {
        return fallbackLanguages().firstOrNull { it.mcCode == mcCode }?.providerCode
            ?: mcCode.substringBefore('_').lowercase()
    }
}

class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)
