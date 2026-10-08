package by.overpas.detekt.intellij.kotlin.numericissues

import kotlin.test.Test
import kotlin.test.assertEquals

class NumericIssuesRuleSetProviderTest {

    private val sut = NumericIssuesRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-numeric-issues id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-numeric-issues", ruleSet.id.value)
    }
}
