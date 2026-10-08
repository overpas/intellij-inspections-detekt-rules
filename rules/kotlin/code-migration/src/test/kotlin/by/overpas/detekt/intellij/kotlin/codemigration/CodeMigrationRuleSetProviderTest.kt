package by.overpas.detekt.intellij.kotlin.codemigration

import kotlin.test.Test
import kotlin.test.assertEquals

class CodeMigrationRuleSetProviderTest {

    private val sut = CodeMigrationRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-code-migration id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-code-migration", ruleSet.id.value)
    }
}
