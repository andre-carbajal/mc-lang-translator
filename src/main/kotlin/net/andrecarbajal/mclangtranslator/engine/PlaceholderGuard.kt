package net.andrecarbajal.mclangtranslator.engine

data class ProtectedText(
    val text: String,
    val placeholders: Map<String, String>,
)

object PlaceholderGuard {
    private val placeholderRegex = Regex("""%(\d+\$)?[%a-zA-Z]|\{[A-Za-z0-9_.-]+}""")

    fun extract(input: String): ProtectedText {
        val placeholders = linkedMapOf<String, String>()
        var index = 0
        val protected = placeholderRegex.replace(input) { match ->
            val token = "__MC_PLACEHOLDER_${index++}__"
            placeholders[token] = match.value
            token
        }
        return ProtectedText(protected, placeholders)
    }

    fun restore(input: String, placeholders: Map<String, String>): String {
        var restored = input
        placeholders.forEach { (token, original) ->
            restored = restored.replace(token, original)
        }
        return restored
    }

    fun warningsFor(original: ProtectedText, translatedProtectedText: String, key: String): List<String> {
        val warnings = mutableListOf<String>()
        original.placeholders.keys.forEach { token ->
            if (!translatedProtectedText.contains(token)) {
                warnings += "$key: missing placeholder token $token"
            }
        }
        val unexpectedTokens = Regex("""__MC_PLACEHOLDER_\d+__""")
            .findAll(translatedProtectedText)
            .map { it.value }
            .filter { it !in original.placeholders.keys }
            .toSet()
        unexpectedTokens.forEach { token -> warnings += "$key: unexpected placeholder token $token" }
        return warnings
    }
}

