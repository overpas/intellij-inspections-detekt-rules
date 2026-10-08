package by.overpas.detekt.intellij.kotlin.probablebugs

import kotlin.test.Test
import kotlin.test.assertEquals

class ProbableBugsRuleSetProviderTest {

    private val sut = ProbableBugsRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-probable-bugs id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-probable-bugs", ruleSet.id.value)
    }
}
