package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceMapGetOrDefaultTest {

    private val environment = createEnvironment()

    private val sut = ReplaceMapGetOrDefault(Config.empty)

    @Test
    fun `a getOrDefault call on a map with non-null values is reported`() {
        val code = """
            fun test(map: Map<Int, String>) {
                map.getOrDefault(1, "bar")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getOrDefault call inside an expression is reported`() {
        val code = """
            fun test(map: Map<Int, String>) = map.getOrDefault(1, "bar") + "baz"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getOrDefault call on a map created by mapOf is reported`() {
        val code = """
            fun test(): String {
                val map = mapOf(1 to "a", 2 to "b")
                return map.getOrDefault(3, "c")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getOrDefault call on a map with nullable values passes`() {
        val code = """
            fun test(): Boolean {
                val map = mapOf(1 to "", 2 to null)
                return map.getOrDefault(2, "bar") == null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getOrDefault function of another class passes`() {
        val code = """
            class Store {
                fun getOrDefault(key: Int, default: String): String = default
            }

            fun test(store: Store) = store.getOrDefault(1, "bar")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an indexing with elvis passes`() {
        val code = """
            fun test(map: Map<Int, String>) = map[1] ?: "bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
