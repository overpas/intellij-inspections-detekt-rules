package by.overpas.detekt.intellij.redundantconstructs

import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantConstructsRuleSetProviderTest {

    private val sut = RedundantConstructsRuleSetProvider()

    @Test
    fun `rule set has the intellij-redundant-constructs id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-redundant-constructs", ruleSet.id.value)
    }
}
