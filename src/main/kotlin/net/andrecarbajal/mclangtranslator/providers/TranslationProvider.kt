package net.andrecarbajal.mclangtranslator.providers

data class McLocale(
    var mcCode: String = "",
    var providerCode: String = "",
    var displayName: String = "",
)

data class TranslationContext(
    val jsonKey: String = "",
    val sourceMcLocale: String = "",
    val targetMcLocale: String = "",
) {
    fun minecraftHint(): String {
        val keyHint = if (jsonKey.isBlank()) "unknown" else jsonKey
        return "Minecraft mod localization JSON key: $keyHint. " +
                "Translate the value as an in-game item, block, UI label, advancement, or tooltip; not casual speech."
    }
}

interface TranslationProvider {
    val key: String
    val displayName: String

    fun getSupportedLanguages(): List<McLocale>

    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        context: TranslationContext = TranslationContext(),
    ): String

    fun fallbackLanguages(): List<McLocale>

    fun providerCodeForMcCode(mcCode: String): String {
        return fallbackLanguages().firstOrNull { it.mcCode == mcCode }?.providerCode
            ?: mcCode.substringBefore('_').lowercase()
    }
}

class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)
