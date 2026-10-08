package by.overpas.detekt.intellij.kotlin.coroutines

import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutinesRuleSetProviderTest {

    private val sut = CoroutinesRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-coroutines id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-coroutines", ruleSet.id.value)
    }
}
