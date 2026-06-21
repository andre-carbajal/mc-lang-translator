package net.andrecarbajal.mclangtranslator.engine

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFile
import net.andrecarbajal.mclangtranslator.providers.McLocale
import net.andrecarbajal.mclangtranslator.providers.TranslationException
import net.andrecarbajal.mclangtranslator.providers.TranslationProvider
import net.andrecarbajal.mclangtranslator.state.McTranslatorState
import java.nio.file.Path

data class TranslationJob(
    val sourceFile: VirtualFile,
    val targetLocales: List<McLocale>,
    val provider: TranslationProvider,
    val sourceLocale: String,
    val settings: McTranslatorState,
)

data class TranslationResult(
    val writtenFiles: List<Path>,
    val warnings: List<String>,
)

class TranslationEngine {
    fun run(
        job: TranslationJob,
        indicator: ProgressIndicator,
        onProgress: (String, Int, Int) -> Unit,
    ): TranslationResult {
        val source = JsonProcessor.read(job.sourceFile)
        val warnings = mutableListOf<String>()
        val writtenFiles = mutableListOf<Path>()
        val total = source.size * job.targetLocales.size
        var completed = 0
        val sourceProviderCode = job.provider.providerCodeForMcCode(job.sourceLocale)

        job.targetLocales.forEach { targetLocale ->
            checkCanceled(indicator)
            val outputPath = job.sourceFile.parent.toNioPath().resolve("${targetLocale.mcCode}.json")
            val translated =
                if (job.settings.skipExistingKeys) JsonProcessor.readIfExists(outputPath) else linkedMapOf()

            source.forEach { (key, value) ->
                checkCanceled(indicator)
                completed += 1
                onProgress(targetLocale.mcCode, completed, total.coerceAtLeast(1))
                if (job.settings.skipExistingKeys && translated.containsKey(key)) return@forEach

                val protected = PlaceholderGuard.extract(value)
                if (protected.text.isBlank()) {
                    translated[key] = PlaceholderGuard.restore(protected.text, protected.placeholders)
                    return@forEach
                }

                val translatedProtected = try {
                    translateWithRetry(
                        provider = job.provider,
                        text = protected.text,
                        sourceLang = sourceProviderCode,
                        targetLang = targetLocale.providerCode,
                        delayMs = job.settings.requestDelayMs,
                        indicator = indicator,
                    )
                } catch (e: Exception) {
                    warnings += "${targetLocale.mcCode}/$key: ${e.message ?: e::class.java.simpleName}"
                    protected.text
                }
                warnings += PlaceholderGuard.warningsFor(protected, translatedProtected, "${targetLocale.mcCode}/$key")
                translated[key] = PlaceholderGuard.restore(translatedProtected, protected.placeholders)
            }
            writtenFiles.add(JsonProcessor.write(job.sourceFile, targetLocale.mcCode, translated))
        }

        return TranslationResult(writtenFiles, warnings)
    }

    private fun translateWithRetry(
        provider: TranslationProvider,
        text: String,
        sourceLang: String,
        targetLang: String,
        delayMs: Long,
        indicator: ProgressIndicator,
    ): String {
        var waitMs = delayMs.coerceAtLeast(0L)
        var lastError: Throwable? = null
        repeat(3) { attempt ->
            checkCanceled(indicator)
            if (waitMs > 0) Thread.sleep(waitMs)
            try {
                return provider.translate(text, sourceLang, targetLang)
            } catch (e: TranslationException) {
                lastError = e
                waitMs = if (waitMs == 0L) 250L else waitMs * 2
                if (attempt == 2) throw e
            }
        }
        throw TranslationException(lastError?.message ?: "Translation failed", lastError)
    }

    private fun checkCanceled(indicator: ProgressIndicator) {
        if (indicator.isCanceled) throw ProcessCanceledException()
        indicator.checkCanceled()
    }
}

fun sourceLocaleFromFileName(fileName: String): String {
    val name = fileName.substringBeforeLast('.', fileName)
    return if (Regex("""[a-z]{2,3}(_[a-z0-9]{2,4})?""").matches(name)) name else "en_us"
}
