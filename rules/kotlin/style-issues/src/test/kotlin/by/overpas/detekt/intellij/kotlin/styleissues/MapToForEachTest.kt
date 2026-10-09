package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MapToForEachTest {

    private val environment = createEnvironment()

    private val sut = MapToForEach(Config.empty)

    @Test
    fun `an unused map call on an iterable is reported`() {
        val code = """
            fun foo(nums: Iterable<Int>) {
                nums.map { println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call with a lambda in parentheses is reported`() {
        val code = """
            fun foo(nums: List<Int>) {
                nums.map({ it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call with a function reference is reported`() {
        val code = """
            fun foo(nums: List<Int>) {
                nums.map(::println)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call with explicit type arguments is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).map<Int, Unit> { print(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call with an implicit non-unit result is reported`() {
        val code = """
            fun foo(nums: List<Int>) {
                nums.map {
                    if (it % 2 == 0) {
                        "even"
                    } else {
                        "odd"
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call with labeled unit returns is reported`() {
        val code = """
            fun foo(nums: List<Int>) {
                nums.map {
                    if (it % 2 == 0) {
                        println("even")
                        return@map
                    } else {
                        println("odd")
                        return@map
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call with an anonymous function is reported`() {
        val code = """
            fun foo(nums: Iterable<Int>) {
                nums.map(
                    fun(it: Int) {
                        if (it > 10) return
                        print(it)
                    },
                )
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused map call through an import alias is reported`() {
        val code = """
            import kotlin.collections.map as transform

            fun foo(nums: List<Int>) {
                nums.transform { it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `unused mapIndexed and mapNotNull calls are reported`() {
        val code = $$"""
            fun test(list: List<String>) {
                list.mapIndexed { index, string -> println("$index: $string") }
                list.mapNotNull { string -> println(string) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `unused onEach and onEachIndexed calls are reported`() {
        val code = $$"""
            fun test(list: List<String>) {
                list.onEach { string -> println(string) }
                list.onEachIndexed { index, string -> println("$index: $string") }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a map call with labeled non-unit returns passes`() {
        val code = """
            fun foo(nums: List<Int>) {
                nums.map {
                    if (it % 2 == 0) {
                        return@map "even"
                    } else {
                        return@map "odd"
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom map function passes`() {
        val code = """
            fun List<Int>.map(transform: (Int) -> Int): List<Int> = listOf(1, 2, 3)

            fun foo() {
                listOf(1, 2, 3).map { it * it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map call on a sequence passes`() {
        val code = """
            fun foo(nums: Sequence<Int>) {
                nums.map { println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map call that is the receiver of another call passes`() {
        val code = """
            fun foo(nums: List<Int>) {
                println(nums.map { it }.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map call used as an argument passes`() {
        val code = """
            fun foo(nums: List<Int>) {
                consume(nums.map { it })
            }

            fun consume(map: List<Int>) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map call used as the function body passes`() {
        val code = """
            fun foo(nums: List<Int>) = nums.map(::println)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map call that is the result of a lambda passes`() {
        val code = """
            fun foo(nums: List<Int>): List<Int> =
                run {
                    nums.map { it + 1 }
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
