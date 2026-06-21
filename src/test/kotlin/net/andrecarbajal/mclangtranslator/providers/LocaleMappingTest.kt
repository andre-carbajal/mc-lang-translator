package net.andrecarbajal.mclangtranslator.providers

import junit.framework.TestCase.assertEquals
import org.junit.Test

class LocaleMappingTest {
    @Test
    fun `normalizes common provider codes to minecraft locale codes`() {
        assertEquals("es_es", toMcCode("es"))
        assertEquals("es_es", toMcCode("es-ES"))
        assertEquals("es_mx", toMcCode("es-419"))
        assertEquals("es_mx", toMcCode("es-MX"))
        assertEquals("pt_br", toMcCode("pt-BR"))
        assertEquals("zh_cn", toMcCode("zh-Hans"))
        assertEquals("zh_tw", toMcCode("zh-Hant"))
    }
}
