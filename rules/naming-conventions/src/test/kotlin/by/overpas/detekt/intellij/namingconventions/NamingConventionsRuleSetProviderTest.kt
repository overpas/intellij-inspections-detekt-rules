package by.overpas.detekt.intellij.namingconventions

import kotlin.test.Test
import kotlin.test.assertEquals

class NamingConventionsRuleSetProviderTest {

    private val sut = NamingConventionsRuleSetProvider()

    @Test
    fun `rule set has the intellij-naming-conventions id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-naming-conventions", ruleSet.id.value)
    }
}
