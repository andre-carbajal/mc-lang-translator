package net.andrecarbajal.mclangtranslator.engine

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import net.andrecarbajal.mclangtranslator.providers.McLocale
import net.andrecarbajal.mclangtranslator.providers.ProviderDefaults
import net.andrecarbajal.mclangtranslator.providers.TranslationContext
import net.andrecarbajal.mclangtranslator.providers.TranslationProvider
import net.andrecarbajal.mclangtranslator.state.McTranslatorState
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class TranslationEngineTest : BasePlatformTestCase() {
    fun testPassesJsonKeyContextToProvider() {
        val langDir = Files.createTempDirectory("mc-lang-translator-test").resolve("assets/demo/lang")
        Files.createDirectories(langDir)
        val sourcePath = langDir.resolve("en_us.json")
        Files.writeString(
            sourcePath,
            """{"item.demo.honey":"Honey","block.demo.honey_block":"Honey Block"}""",
            StandardCharsets.UTF_8,
        )
        val file = checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(sourcePath))
        val provider = CapturingProvider()

        TranslationEngine().run(
            job = TranslationJob(
                sourceFile = file,
                targetLocales = listOf(McLocale("es_es", "es", "Spanish")),
                provider = provider,
                sourceLocale = "en_us",
                settings = McTranslatorState(),
            ),
            indicator = EmptyProgressIndicator(),
            onProgress = { _, _, _ -> },
        )

        assertEquals(listOf("item.demo.honey", "block.demo.honey_block"), provider.contexts.map { it.jsonKey })
        assertEquals(listOf("es_es", "es_es"), provider.contexts.map { it.targetMcLocale })
        assertEquals(listOf("en_us", "en_us"), provider.contexts.map { it.sourceMcLocale })
    }

    private class CapturingProvider : TranslationProvider {
        val contexts = mutableListOf<TranslationContext>()

        override val key = "fake"
        override val displayName = "Fake"

        override fun getSupportedLanguages(): List<McLocale> = fallbackLanguages()

        override fun translate(
            text: String,
            sourceLang: String,
            targetLang: String,
            context: TranslationContext,
        ): String {
            contexts += context
            return text
        }

        override fun fallbackLanguages(): List<McLocale> = ProviderDefaults.commonLocales
    }
}
