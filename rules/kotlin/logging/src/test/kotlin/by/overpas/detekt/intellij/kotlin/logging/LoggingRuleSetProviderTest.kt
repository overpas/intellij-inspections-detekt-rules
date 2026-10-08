package by.overpas.detekt.intellij.kotlin.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class LoggingRuleSetProviderTest {

    private val sut = LoggingRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-logging id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-logging", ruleSet.id.value)
    }
}
