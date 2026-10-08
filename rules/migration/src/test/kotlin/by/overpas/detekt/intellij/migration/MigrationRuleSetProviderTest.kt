package by.overpas.detekt.intellij.migration

import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationRuleSetProviderTest {

    private val sut = MigrationRuleSetProvider()

    @Test
    fun `rule set has the intellij-migration id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-migration", ruleSet.id.value)
    }
}
