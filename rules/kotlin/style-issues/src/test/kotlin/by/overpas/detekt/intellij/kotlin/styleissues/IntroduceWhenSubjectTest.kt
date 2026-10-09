package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class IntroduceWhenSubjectTest {

    private val environment = createEnvironment()

    private val sut = IntroduceWhenSubject(Config.empty)

    @Test
    fun `a when with equality tests of the same value is reported`() {
        val code = """
            fun test(n: Int): String {
                return when {
                    n == 0 -> "zero"
                    1 == n -> "one"
                    n == 2 -> "two"
                    else -> "unknown"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with type checks of the same value is reported`() {
        val code = """
            class Klass<T>

            fun test(obj: Any): String {
                return when {
                    obj is String -> "string"
                    obj !is Klass<*> -> "not class"
                    else -> "unknown"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with range tests of the same value is reported`() {
        val code = """
            fun test(n: Int): String {
                return when {
                    n in 0..10 -> "small"
                    n !in 0..100 -> "big"
                    else -> "average"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with alternatives and object comparisons is reported`() {
        val code = """
            interface Foo {
                val origin: Origin
            }

            sealed class Origin
            object Src : Origin()
            object Lib : Origin()
            object Sdk : Origin()

            val Foo.bar: Boolean
                get() = when {
                    origin == Src -> false
                    origin == Lib || origin == Sdk -> false
                    else -> true
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with qualified subjects and constants is reported`() {
        val code = """
            class Foo {
                val bar = 1
            }

            const val A = 1
            const val B = 2

            fun test(foo: Foo): Int {
                return when {
                    A == foo.bar -> 10
                    B == foo.bar -> 20
                    else -> 30
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with type checks of this is reported`() {
        val code = """
            fun Number.test() {
                when {
                    this is Int -> {}
                    this is Long -> {}
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with guard conditions is reported`() {
        val code = """
            fun test(a: Any) {
                when {
                    a is String && a.isNotEmpty() -> Unit
                    a is Int && (a > 0 || a < -10) -> Unit
                    else -> Unit
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with a single regular branch passes`() {
        val code = """
            fun foo(a: Any, b: Any) {
                when {
                    a == b -> true
                    else -> false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when with a subject passes`() {
        val code = """
            fun test(n: Int): String {
                return when (n) {
                    in 0..10 -> "small"
                    in 10..100 -> "average"
                    else -> "unknown"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when with comparisons that cannot become branch conditions passes`() {
        val code = """
            fun test(n: Int): String {
                return when {
                    n in 0..10 -> "small"
                    n >= 10 && n <= 100 -> "average"
                    n < 0 || n > 1000 -> "unknown"
                    else -> "big"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when that checks different values passes`() {
        val code = """
            fun test(n: Int): String {
                return when {
                    n in 0..10 -> "n is small"
                    n / 10 in 0..10 -> "n is average"
                    n / 100 in 0..10 -> "n is big"
                    else -> "unknown"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when that mixes alternatives and guards passes`() {
        val code = """
            fun test(a: Any) {
                when {
                    a is String && a.isNotEmpty() || a is Int -> Unit
                    a is Long -> Unit
                    else -> Unit
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when with parenthesized guard conditions passes`() {
        val code = """
            fun test(a: Any) {
                when {
                    ((a is String && a.isNotEmpty()) && a[0] == '0') -> Unit
                    a is Int -> Unit
                    else -> Unit
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when that evaluates a custom getter more than once passes`() {
        val code = """
            class Counter {
                var count = 0
                val next: Int
                    get() = ++count
            }

            fun test(counter: Counter): String {
                return when {
                    counter.next == 1 -> "one"
                    counter.next == 2 -> "two"
                    else -> "many"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
