package by.overpas.detekt.intellij.otherproblems

import kotlin.test.Test
import kotlin.test.assertEquals

class OtherProblemsRuleSetProviderTest {

    private val sut = OtherProblemsRuleSetProvider()

    @Test
    fun `rule set has the intellij-other-problems id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-other-problems", ruleSet.id.value)
    }
}
