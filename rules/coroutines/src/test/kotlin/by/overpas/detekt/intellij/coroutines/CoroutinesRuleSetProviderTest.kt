package by.overpas.detekt.intellij.coroutines

import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutinesRuleSetProviderTest {

    private val sut = CoroutinesRuleSetProvider()

    @Test
    fun `rule set has the intellij-coroutines id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-coroutines", ruleSet.id.value)
    }
}
