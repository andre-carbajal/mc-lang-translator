<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# MC Lang Translator Changelog

## [1.0.1]

### Changed

- Configure GitHub Actions publishing from version tags.
- Use an explicit actions resource bundle and globally unique action ID.
- Move translation action UI strings to the resource bundle.
- Mark the translation action as dumb-aware so it remains valid during IDE indexing.

### Fixed

- Fix Marketplace/DevKit action metadata warnings for localized action text and description.
- Fix plugin description generation by using the README as the Marketplace description.

## [1.0.0]

### Added

- Add plugin icon assets for light and dark IntelliJ plugin listings.
- Add the initial MC Lang Translator IntelliJ plugin implementation.
- Add `Translate lang file...` to the Project View context menu for `.json` files inside `lang` folders.
- Add translation support for flat Minecraft language JSON files, writing translated locale files beside the source file.
- Add provider support for Google Translate, Microsoft Translator, DeepL, and OpenAI-compatible AI APIs.
- Add settings UI for provider selection, API keys, DeepL options, Microsoft region, AI endpoint/model, request delay, and skipping existing keys.
- Store provider API keys in IntelliJ PasswordSafe.
- Add target-language selection UI with provider language loading and fallback locale lists.
- Preserve Minecraft placeholders such as `%s`, `%1$s`, `%%`, and `{name}` during translation.
- Add progress reporting, cancellation support, retry handling, and translation warnings.
- Add locale mapping coverage for Minecraft-style language codes, including additional Spanish variants.
- Add DeepL SDK integration with client adapter, fallback language handling, and tests.
- Add structured translation context using JSON keys and Minecraft locale metadata.
- Add AI prompt context so ambiguous strings such as item or block names are translated as Minecraft localization text.
- Add DeepL context support using the provider's formal context option.

### Changed

- Replace the IntelliJ Platform Plugin Template sample code with the MC Lang Translator plugin codebase.
- Keep Google Translate and Microsoft Translator requests limited to the source text while still passing JSON-key context through the provider contract.
- Update README content to describe MC Lang Translator usage and build steps.

### Fixed

- Improve Spanish locale mapping for Google Translate provider codes.
- Add tests for action visibility, source locale detection, placeholder preservation, locale mapping, DeepL behavior, translation context propagation, and AI prompt context.

### Removed

- Remove unused IntelliJ template sample services, tool window, bundle resources, and template tests.
