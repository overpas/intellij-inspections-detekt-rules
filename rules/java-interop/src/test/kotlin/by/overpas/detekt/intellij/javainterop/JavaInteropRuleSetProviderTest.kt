package by.overpas.detekt.intellij.javainterop

import kotlin.test.Test
import kotlin.test.assertEquals

class JavaInteropRuleSetProviderTest {

    private val sut = JavaInteropRuleSetProvider()

    @Test
    fun `rule set has the intellij-java-interop id`() {
        val ruleSet = sut.instance()

        assertEquals("intellij-java-interop", ruleSet.id.value)
    }
}
