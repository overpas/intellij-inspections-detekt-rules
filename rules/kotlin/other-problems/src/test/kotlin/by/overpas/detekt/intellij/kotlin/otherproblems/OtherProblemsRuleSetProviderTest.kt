package by.overpas.detekt.intellij.kotlin.otherproblems

import kotlin.test.Test
import kotlin.test.assertEquals

class OtherProblemsRuleSetProviderTest {

    private val sut = OtherProblemsRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-other-problems id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-other-problems", ruleSet.id.value)
    }
}
