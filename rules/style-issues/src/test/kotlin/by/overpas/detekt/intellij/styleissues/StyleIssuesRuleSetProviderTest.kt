package by.overpas.detekt.intellij.styleissues

import kotlin.test.Test
import kotlin.test.assertEquals

class StyleIssuesRuleSetProviderTest {

    private val sut = StyleIssuesRuleSetProvider()

    @Test
    fun `rule set has the intellij-style-issues id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-style-issues", ruleSet.id.value)
    }
}
