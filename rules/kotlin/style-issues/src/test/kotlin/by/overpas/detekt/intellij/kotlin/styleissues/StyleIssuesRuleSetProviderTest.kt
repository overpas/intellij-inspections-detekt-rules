package by.overpas.detekt.intellij.kotlin.styleissues

import kotlin.test.Test
import kotlin.test.assertEquals

class StyleIssuesRuleSetProviderTest {

    private val sut = StyleIssuesRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-style-issues id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-style-issues", ruleSet.id.value)
    }
}
