package net.andrecarbajal.mclangtranslator.providers

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AiProviderTest {
    @Test
    fun `system prompt appends minecraft context rules to custom prompt`() {
        val provider = AiProvider(
            apiKey = "fake-key",
            endpointUrl = "https://example.test/chat",
            modelName = "test-model",
            systemPromptOverride = "Use concise translations.",
        )
        val context = TranslationContext(
            jsonKey = "item.demo.honey",
            sourceMcLocale = "en_us",
            targetMcLocale = "es_es",
        )

        val prompt = provider.buildSystemPrompt("Use concise translations.", context)

        assertTrue(prompt.contains("Use concise translations."))
        assertTrue(prompt.contains("item.demo.honey"))
        assertTrue(prompt.contains("Minecraft mod localization JSON key"))
        assertTrue(prompt.contains("Return only the translated text"))
        assertTrue(prompt.contains("__MC_PLACEHOLDER_0__"))
    }

    @Test
    fun `user message includes key locales and text without translating instructions`() {
        val provider = AiProvider(
            apiKey = "fake-key",
            endpointUrl = "https://example.test/chat",
            modelName = "test-model",
            systemPromptOverride = "",
        )
        val context = TranslationContext(
            jsonKey = "item.demo.honey",
            sourceMcLocale = "en_us",
            targetMcLocale = "es_es",
        )

        val message = provider.buildUserMessage("Honey", "en", "es", context)

        assertTrue(message.contains("JSON key: item.demo.honey"))
        assertTrue(message.contains("Source Minecraft locale: en_us"))
        assertTrue(message.contains("Target Minecraft locale: es_es"))
        assertTrue(message.endsWith("Honey"))
        assertFalse(message.contains("cariño", ignoreCase = true))
    }
}
