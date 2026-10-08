package by.overpas.detekt.intellij.codemigration

import kotlin.test.Test
import kotlin.test.assertEquals

class CodeMigrationRuleSetProviderTest {

    private val sut = CodeMigrationRuleSetProvider()

    @Test
    fun `rule set has the intellij-code-migration id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-code-migration", ruleSet.id.value)
    }
}
