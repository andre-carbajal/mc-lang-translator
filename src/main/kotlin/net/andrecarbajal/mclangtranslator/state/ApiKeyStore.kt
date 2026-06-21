package net.andrecarbajal.mclangtranslator.state

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe

object ApiKeyStore {
    private const val PREFIX = "MC Lang Translator"

    fun get(providerKey: String): String =
        PasswordSafe.instance.getPassword(attributes(providerKey)) ?: ""

    fun set(providerKey: String, apiKey: String) {
        PasswordSafe.instance.setPassword(attributes(providerKey), apiKey)
    }

    private fun attributes(providerKey: String): CredentialAttributes =
        CredentialAttributes("$PREFIX:$providerKey", "apiKey")
}

