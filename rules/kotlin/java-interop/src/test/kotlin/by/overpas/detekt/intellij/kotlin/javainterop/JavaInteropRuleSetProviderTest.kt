package by.overpas.detekt.intellij.kotlin.javainterop

import kotlin.test.Test
import kotlin.test.assertEquals

class JavaInteropRuleSetProviderTest {

    private val sut = JavaInteropRuleSetProvider()

    @Test
    fun `rule set has the intellij-kotlin-java-interop id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-kotlin-java-interop", ruleSet.id.value)
    }
}
