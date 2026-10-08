package by.overpas.detekt.intellij.kotlin.namingconventions

import kotlin.test.Test
import kotlin.test.assertEquals

class NamingConventionsRuleSetProviderTest {

    private val sut = NamingConventionsRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-naming-conventions id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-naming-conventions", ruleSet.id.value)
    }
}
