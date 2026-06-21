package net.andrecarbajal.mclangtranslator.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "McTranslatorState", storages = [Storage("mc-lang-translator.xml")])
class McTranslatorStateService : PersistentStateComponent<McTranslatorState> {
    private var state = McTranslatorState()

    override fun getState(): McTranslatorState = state

    override fun loadState(state: McTranslatorState) {
        this.state = state
    }

    companion object {
        fun getInstance(): McTranslatorStateService =
            ApplicationManager.getApplication().getService(McTranslatorStateService::class.java)

        fun settings(): McTranslatorState = getInstance().state
    }
}

