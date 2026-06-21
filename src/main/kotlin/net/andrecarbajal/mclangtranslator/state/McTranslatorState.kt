package net.andrecarbajal.mclangtranslator.state

data class McTranslatorState(
    var lastUsedProvider: String = "google",
    var lastUsedLanguages: MutableList<String> = mutableListOf("es_es"),
    var requestDelayMs: Long = 50L,
    var skipExistingKeys: Boolean = false,
    var microsoftRegion: String = "global",
    var deeplApiHost: String = "https://api-free.deepl.com",
    var deeplFormality: String = "default",
    var aiEndpointUrl: String = "https://api.openai.com/v1/chat/completions",
    var aiModelName: String = "gpt-4o",
    var aiSystemPromptOverride: String = "",
)

