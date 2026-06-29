# MC Lang Translator

MC Lang Translator is an IntelliJ IDEA plugin for translating Minecraft mod language files.

It is designed for mod projects that keep localization files under paths such as
`src/main/resources/assets/<modid>/lang/en_us.json`. From the Project View, you can translate one
source language file into one or more Minecraft locale files without leaving the IDE.

## What It Does

- Adds `Translate lang file...` to the context menu for `.json` files inside a folder named `lang`.
- Translates flat Minecraft language JSON files, such as `en_us.json`, into target locale files like `es_es.json`.
- Writes translated files beside the source file, keeping the usual Minecraft resource-pack layout.
- Supports Google Translate, DeepL, Microsoft Translator, and OpenAI-compatible AI APIs.
- Uses the JSON key as translation context, which helps ambiguous Minecraft terms such as item, block, UI, tooltip, and advancement names.
- Preserves Minecraft placeholders such as `%s`, `%1$s`, `%%`, and `{name}`.
- Can skip keys that already exist in a target file, which is useful when updating existing translations.
- Stores API keys in IntelliJ PasswordSafe.

## Requirements

- IntelliJ IDEA.
- A Minecraft language JSON file inside a `lang` folder.
- An API key for the translation provider you want to use.

The source JSON must be a flat string-to-string map:

```json
{
  "item.example.honey": "Honey",
  "block.example.honey_block": "Honey Block"
}
```

Nested JSON objects or non-string values are not supported.

## Setup

1. Open `Settings | Tools | MC Lang Translator`.
2. Choose the active provider.
3. Enter the API key for that provider.
4. Adjust provider-specific options if needed:
   - DeepL API host and formality.
   - Microsoft region.
   - AI endpoint URL, model, and optional system prompt.
5. Optionally enable `Skip keys that already exist in the target file`.
6. Use `Test connection` to confirm the provider can load languages.

## Usage

1. In the Project View, right-click a file like `assets/example/lang/en_us.json`.
2. Choose `Translate lang file...`.
3. Select one or more target languages.
4. Confirm the dialog.
5. Wait for the background task to finish.

For each selected language, the plugin writes a file in the same `lang` folder:

```text
assets/example/lang/en_us.json
assets/example/lang/es_es.json
assets/example/lang/fr_fr.json
```

If a translation request fails for a key, the plugin keeps the original text for that key and reports a warning.

## Notes For AI Translation

The AI provider sends Minecraft-specific context with each string, including the JSON key and source/target locales. This helps the model translate terms as in-game content instead of casual speech. For example, a key like `item.example.honey` gives the model context that `Honey` should be translated as an item name.

Custom AI system prompts are supported. The plugin still appends required Minecraft localization rules so placeholders and output format stay safe.

## Development

Run tests:

```powershell
.\gradlew.bat test
```

Build the installable plugin ZIP:

```powershell
.\gradlew.bat buildPlugin
```

The ZIP is created under `build/distributions/`.
