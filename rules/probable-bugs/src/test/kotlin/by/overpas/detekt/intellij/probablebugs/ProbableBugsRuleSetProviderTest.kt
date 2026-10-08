package by.overpas.detekt.intellij.probablebugs

import kotlin.test.Test
import kotlin.test.assertEquals

class ProbableBugsRuleSetProviderTest {

    private val sut = ProbableBugsRuleSetProvider()

    @Test
    fun `rule set has the intellij-probable-bugs id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-probable-bugs", ruleSet.id.value)
    }
}
