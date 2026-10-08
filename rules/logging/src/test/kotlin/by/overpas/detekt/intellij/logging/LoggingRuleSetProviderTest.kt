package by.overpas.detekt.intellij.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class LoggingRuleSetProviderTest {

    private val sut = LoggingRuleSetProvider()

    @Test
    fun `rule set has the intellij-logging id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-logging", ruleSet.id.value)
    }
}
