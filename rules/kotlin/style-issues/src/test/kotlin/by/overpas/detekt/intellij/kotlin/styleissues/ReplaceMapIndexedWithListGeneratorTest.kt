package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceMapIndexedWithListGeneratorTest {

    private val environment = createEnvironment()

    private val sut = ReplaceMapIndexedWithListGenerator(Config.empty)

    @Test
    fun `a mapIndexed call with an underscore element parameter is reported`() {
        val code = """
            fun test(list: List<String>) = list.mapIndexed { index, _ ->
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call with an unused element parameter is reported`() {
        val code = """
            fun test(list: List<String>) = list.mapIndexed { index, value ->
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call with an anonymous function is reported`() {
        val code = """
            fun test() = emptyList<String>().mapIndexed(fun(index: Int, _: String): Int {
                return index
            })
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call on an implicit receiver is reported`() {
        val code = """
            fun List<String>.test() = mapIndexed { index, _ ->
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call with the lambda inside parentheses is reported`() {
        val code = """
            fun test(list: List<String>) = list.mapIndexed({ index, _ -> index + 42 })
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call through an import alias is reported`() {
        val code = """
            import kotlin.collections.mapIndexed as foo

            fun test(list: List<String>) = list.foo { index, _ ->
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call with a labeled lambda is reported`() {
        val code = """
            fun test(list: List<String>) = list.mapIndexed label@{ index, value ->
                if (index == 0) return@label 0
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call with a labeled return of Unit is reported`() {
        val code = """
            fun test(list: List<String>) {
                list.mapIndexed { index, value ->
                    if (index == 0) return@mapIndexed
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed call that uses the element passes`() {
        val code = """
            fun test(list: List<String>) = list.mapIndexed { index, value ->
                println(value)
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mapIndexed call with an anonymous function that uses the element passes`() {
        val code = """
            fun test() = emptyList<String>().mapIndexed(fun(index: Int, value: String) {
                println(value)
                index
            })
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mapIndexed call that uses a destructured element passes`() {
        val code = """
            fun test() = emptyList<Pair<Int, Int>>().mapIndexed { index, (l, r) ->
                l + r + index
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mapIndexed call on an iterable passes`() {
        val code = """
            fun test(list: Iterable<String>) = list.mapIndexed { index, _ ->
                index + 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
