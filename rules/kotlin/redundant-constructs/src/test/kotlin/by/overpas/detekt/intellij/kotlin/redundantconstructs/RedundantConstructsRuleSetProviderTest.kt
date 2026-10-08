package by.overpas.detekt.intellij.kotlin.redundantconstructs

import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantConstructsRuleSetProviderTest {

    private val sut = RedundantConstructsRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-redundant-constructs id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-redundant-constructs", ruleSet.id.value)
    }
}
