package by.overpas.detekt.intellij.kotlin.migration

import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationRuleSetProviderTest {

    private val sut = MigrationRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-migration id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-migration", ruleSet.id.value)
    }
}
