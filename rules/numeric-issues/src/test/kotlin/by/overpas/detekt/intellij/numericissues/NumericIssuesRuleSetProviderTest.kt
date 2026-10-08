package by.overpas.detekt.intellij.numericissues

import kotlin.test.Test
import kotlin.test.assertEquals

class NumericIssuesRuleSetProviderTest {

    private val sut = NumericIssuesRuleSetProvider()

    @Test
    fun `rule set has the intellij-numeric-issues id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-numeric-issues", ruleSet.id.value)
    }
}
