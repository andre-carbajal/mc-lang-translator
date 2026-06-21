package net.andrecarbajal.mclangtranslator.cache

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import net.andrecarbajal.mclangtranslator.providers.McLocale

data class CachedLanguageList(
    var languages: MutableList<McLocale> = mutableListOf(),
    var fetchedAt: Long = 0L,
    var providerKey: String = "",
)

data class ProviderLanguageCacheState(
    var cache: MutableMap<String, CachedLanguageList> = mutableMapOf(),
)

@Service(Service.Level.APP)
@State(name = "McTranslatorLanguageCache", storages = [Storage("mc-lang-translator-languages.xml")])
class ProviderLanguageCache : PersistentStateComponent<ProviderLanguageCacheState> {
    private var state = ProviderLanguageCacheState()

    fun get(providerKey: String): List<McLocale>? {
        val cached = state.cache[providerKey] ?: return null
        val age = System.currentTimeMillis() - cached.fetchedAt
        return if (age <= TTL_MS) cached.languages else null
    }

    fun put(providerKey: String, languages: List<McLocale>) {
        state.cache[providerKey] =
            CachedLanguageList(languages.toMutableList(), System.currentTimeMillis(), providerKey)
    }

    fun invalidate(providerKey: String) {
        state.cache.remove(providerKey)
    }

    override fun getState(): ProviderLanguageCacheState = state

    override fun loadState(state: ProviderLanguageCacheState) {
        this.state = state
    }

    companion object {
        const val TTL_MS = 24 * 60 * 60 * 1000L

        fun getInstance(): ProviderLanguageCache =
            ApplicationManager.getApplication().getService(ProviderLanguageCache::class.java)
    }
}

