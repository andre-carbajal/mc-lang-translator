# MC Lang Translator

IntelliJ IDEA plugin for translating Minecraft mod language files.

## Features

- Adds `Translate lang file...` to the Project View context menu for `.json` files inside a folder named `lang`.
- Translates flat Minecraft language JSON files from one locale to one or more target locales.
- Supports Google Translate, DeepL, Microsoft Translator, and OpenAI-compatible AI APIs.
- Preserves Minecraft-style placeholders such as `%s`, `%1$s`, `%%`, and `{name}`.
- Writes translated files beside the source file as `<locale>.json`, for example `es_es.json`.
- Stores API keys in IntelliJ PasswordSafe instead of persisted XML state.

## Usage

1. Open `Settings | Tools | MC Lang Translator`.
2. Choose the active provider and configure its API key.
3. Right-click a file such as `assets/example/lang/en_us.json`.
4. Choose `Translate lang file...`.
5. Select target languages and confirm.

## Build

```powershell
.\gradlew.bat test
.\gradlew.bat buildPlugin
```

The installable plugin ZIP is created under `build/distributions/`.
