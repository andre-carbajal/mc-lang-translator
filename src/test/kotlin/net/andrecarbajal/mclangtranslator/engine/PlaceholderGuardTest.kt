package net.andrecarbajal.mclangtranslator.engine

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PlaceholderGuardTest {
    @Test
    fun `extracts and restores minecraft placeholders`() {
        val protected = PlaceholderGuard.extract("Use %1\$s on {target} for 50%% power")

        assertEquals(
            "Use __MC_PLACEHOLDER_0__ on __MC_PLACEHOLDER_1__ for 50__MC_PLACEHOLDER_2__ power",
            protected.text,
        )
        assertEquals("Use %1\$s on {target} for 50%% power", PlaceholderGuard.restore(protected.text, protected.placeholders))
    }

    @Test
    fun `warns when translated text drops placeholder token`() {
        val protected = PlaceholderGuard.extract("Hello %s")

        val warnings = PlaceholderGuard.warningsFor(protected, "Hola", "item.example")

        assertTrue(warnings.single().contains("missing placeholder token"))
    }
}

