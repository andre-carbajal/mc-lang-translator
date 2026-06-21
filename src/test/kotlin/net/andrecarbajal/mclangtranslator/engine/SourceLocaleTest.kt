package net.andrecarbajal.mclangtranslator.engine

import junit.framework.TestCase.assertEquals
import org.junit.Test

class SourceLocaleTest {
    @Test
    fun `infers source locale from minecraft lang filename`() {
        assertEquals("en_us", sourceLocaleFromFileName("en_us.json"))
        assertEquals("lzh", sourceLocaleFromFileName("lzh.json"))
    }

    @Test
    fun `falls back to en_us for unusual names`() {
        assertEquals("en_us", sourceLocaleFromFileName("translations.json"))
    }
}

