package net.andrecarbajal.mclangtranslator.providers

import com.deepl.api.Formality
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DeepLProviderTest {
    @Test
    fun `maps sdk target languages to minecraft locales`() {
        val provider = providerWith(
            languages = listOf(
                DeepLLanguageInfo("ES", "Spanish"),
                DeepLLanguageInfo("PT-BR", "Portuguese (Brazilian)"),
                DeepLLanguageInfo("ZH", "Chinese"),
            ),
        )

        val languages = provider.getSupportedLanguages()

        assertEquals("es_es", languages.first { it.providerCode == "ES" }.mcCode)
        assertEquals("pt_br", languages.first { it.providerCode == "PT-BR" }.mcCode)
        assertEquals("zh_cn", languages.first { it.providerCode == "ZH" }.mcCode)
    }

    @Test
    fun `translate returns sdk text without formality option by default`() {
        val adapter = FakeDeepLClientAdapter(translation = "Hola mundo")
        val provider = providerWith(adapter = adapter, formality = "default")

        val translated = provider.translate("Hello world", "en", "es")

        assertEquals("Hola mundo", translated)
        assertEquals("EN", adapter.lastSourceLang)
        assertEquals("ES", adapter.lastTargetLang)
        assertNull(adapter.lastFormality)
    }

    @Test
    fun `translate passes configured formality option`() {
        val adapter = FakeDeepLClientAdapter(translation = "Buenas tardes")
        val provider = providerWith(adapter = adapter, formality = "prefer_more")

        provider.translate("Good afternoon", "en", "es")

        assertEquals(Formality.PreferMore, adapter.lastFormality)
    }

    @Test
    fun `translate wraps sdk errors in translation exception`() {
        val provider = providerWith(adapter = FakeDeepLClientAdapter(error = IllegalStateException("boom")))

        val error = runCatching { provider.translate("Hello", "en", "es") }.exceptionOrNull()

        assertTrue(error is TranslationException)
        assertTrue(error?.message?.contains("boom") == true)
    }

    @Test
    fun `language loading falls back when sdk fails`() {
        val provider = providerWith(adapter = FakeDeepLClientAdapter(languageError = IllegalStateException("offline")))

        val languages = provider.getSupportedLanguages()

        assertTrue(languages.any { it.mcCode == "es_es" && it.providerCode == "ES" })
    }

    private fun providerWith(
        languages: List<DeepLLanguageInfo> = emptyList(),
        adapter: FakeDeepLClientAdapter = FakeDeepLClientAdapter(languages = languages),
        formality: String = "default",
    ): DeepLProvider = DeepLProvider(
        apiKey = "fake-key",
        apiHost = "https://api-free.deepl.com",
        formality = formality,
        clientAdapter = adapter,
    )

    private class FakeDeepLClientAdapter(
        private val languages: List<DeepLLanguageInfo> = emptyList(),
        private val translation: String = "",
        private val error: Exception? = null,
        private val languageError: Exception? = null,
    ) : DeepLClientAdapter {
        var lastSourceLang: String? = null
        var lastTargetLang: String? = null
        var lastFormality: Formality? = null

        override fun getTargetLanguages(): List<DeepLLanguageInfo> {
            languageError?.let { throw it }
            return languages
        }

        override fun translateText(
            text: String,
            sourceLang: String,
            targetLang: String,
            formality: Formality?,
        ): String {
            error?.let { throw it }
            lastSourceLang = sourceLang
            lastTargetLang = targetLang
            lastFormality = formality
            return translation
        }
    }
}
