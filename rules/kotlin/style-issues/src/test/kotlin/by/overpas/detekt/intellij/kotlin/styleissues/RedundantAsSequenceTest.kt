package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantAsSequenceTest {

    private val environment = createEnvironment()

    private val sut = RedundantAsSequence(Config.empty)

    @Test
    fun `an asSequence call on a list followed by a terminal operation is reported`() {
        val code = """
            fun test(list: List<String>): Boolean {
                return list.asSequence().all { it.isBlank() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an asSequence call on a set followed by a terminal operation is reported`() {
        val code = """
            fun test(set: Set<String>): Boolean {
                return set.asSequence().any { it.isBlank() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an asSequence call followed by a terminal operation on the next line is reported`() {
        val code = """
            fun test(list: List<String>): String? {
                return list
                    .asSequence()
                    .maxOrNull()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe asSequence call followed by a terminal operation is reported`() {
        val code = """
            fun test(list: List<String>?): String? {
                return list?.asSequence()?.first()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an asSequence call on a sequence is reported`() {
        val code = """
            fun test(): Sequence<Int> {
                return sequenceOf(1, 2, 3).asSequence()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an asSequence call on a sequence followed by a transformation is reported`() {
        val code = """
            fun test(): Sequence<Int> {
                return sequenceOf(1, 2, 3).asSequence().map { it + 1 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an asSequence call on a sequence with a redundant type argument is reported`() {
        val code = """
            fun test(a: Sequence<String>): Sequence<String> {
                return a.asSequence<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an asSequence call on a list followed by a transformation passes`() {
        val code = """
            fun test(list: List<String>): String {
                return list.asSequence().sorted().first()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an asSequence call on a list followed by a non-terminal call chain passes`() {
        val code = """
            fun test(list: List<Int>): Int {
                return list.asSequence().runningReduce { acc, i -> acc + i }.take(10).last()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an asSequence call followed by a custom sequence function passes`() {
        val code = """
            fun foo(list: List<Int>): Int? =
                list.asSequence()
                    .runningFold(0) { x, y -> x + y }
                    .foo()
                    .lastOrNull()

            fun Sequence<Int>.foo(): Sequence<Int> = map { it + 1 }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an asSequence call on a map passes`() {
        val code = """
            fun test(map: Map<String, Int>): Map.Entry<String, Int>? {
                return map.asSequence().firstOrNull()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an asSequence call on an iterator passes`() {
        val code = """
            fun test(a: Iterator<String>): Sequence<String> {
                return a.asSequence<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an asSequence call on a sequence with a widening type argument passes`() {
        val code = """
            fun test(a: Sequence<String>): Sequence<CharSequence> {
                return a.asSequence<CharSequence>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an asSequence call on a list without a following call passes`() {
        val code = """
            fun test(list: List<String>): Sequence<String> {
                return list.asSequence()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
