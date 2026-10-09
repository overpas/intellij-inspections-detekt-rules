package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceMapKeysCallChainWithKeysTest {

    private val environment = createEnvironment()

    private val sut = ReplaceMapKeysCallChainWithKeys(Config.empty)

    @Test
    fun `a map of keys with the implicit parameter followed by toSet is reported`() {
        val code = """
            fun test(steps: Map<String, Int>): Set<String> {
                return steps.map { it.key }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map of keys with an explicit parameter followed by toSet is reported`() {
        val code = """
            fun test(steps: Map<String, Int>): Set<String> {
                return steps.map { step -> step.key }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map of keys with an implicit receiver followed by toSet is reported`() {
        val code = """
            fun test(steps: Map<String, Int>): Set<String> = with(steps) {
                map { it.key }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map of keys with safe calls followed by toSet is reported`() {
        val code = """
            fun test(steps: Map<String, Int>?): Set<String>? {
                return steps?.map { it.key }?.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map of keys of the entries followed by toSet passes`() {
        val code = """
            fun test(steps: Map<String, Int>): Set<String> {
                return steps.entries.map { it.key }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map of keys with a comment in the lambda passes`() {
        val code = """
            fun test(steps: Map<String, Int>): Set<String> {
                return steps.map {
                    // Keep this comment.
                    it.key
                }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map of key properties of a list followed by toSet passes`() {
        val code = """
            data class Step(val key: String)

            fun test(steps: List<Step>): Set<String> {
                return steps.map { it.key }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map of keys followed by toList passes`() {
        val code = """
            fun test(steps: Map<String, Int>): List<String> {
                return steps.map { it.key }.toList()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map of values followed by toSet passes`() {
        val code = """
            fun test(steps: Map<String, Int>): Set<Int> {
                return steps.map { it.value }.toSet()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
