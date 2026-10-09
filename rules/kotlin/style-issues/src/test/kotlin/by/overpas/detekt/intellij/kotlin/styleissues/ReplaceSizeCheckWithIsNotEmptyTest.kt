package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceSizeCheckWithIsNotEmptyTest {

    private val environment = createEnvironment()

    private val sut = ReplaceSizeCheckWithIsNotEmpty(Config.empty)

    @Test
    fun `an array size not equal to zero is reported`() {
        val code = """
            fun test() = arrayOf(1, 2, 3).size != 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `zero not equal to a char sequence length is reported`() {
        val code = """
            fun foo(text: CharSequence) = 0 != text.length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `zero less than a collection size is reported`() {
        val code = """
            fun test(items: Collection<Int>) = 0 < items.size
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a collection subtype size greater than or equal to one is reported`() {
        val code = """
            class Foo : AbstractCollection<Int>() {
                override val size = 0
                override fun iterator() = emptyList<Int>().iterator()
            }

            fun test(items: Foo) = items.size >= 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a size check inside an isNotEmpty overload with parameters is reported`() {
        val code = """
            class EmptyCollection<T> : AbstractCollection<T>() {
                override val size: Int
                    get() = 0

                override fun iterator(): Iterator<T> = emptyList<T>().iterator()

                fun isNotEmpty(dummy: Boolean): Boolean = size != 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit string length check is reported`() {
        val code = """
            fun String.test(): Boolean = 0 != length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a size check of a flexible list is reported`() {
        val code = """
            fun test() = java.util.concurrent.atomic.AtomicReference(listOf(1, 2, 3)).get().size > 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map size not equal to zero is reported`() {
        val code = """
            fun test() = mapOf(1 to 2).size != 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a primitive array size greater than zero is reported`() {
        val code = """
            fun test() = intArrayOf(1, 2, 3).size > 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a progression count check is reported`() {
        val code = """
            fun test() = 1 <= (1..9 step 2).count()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a range count check is reported`() {
        val code = """
            fun test() = (1..9).count() >= 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit primitive array size check is reported`() {
        val code = """
            fun test() = doubleArrayOf(1.0, 2.5).run { 0 < size }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type alias array size check is reported`() {
        val code = """
            typealias MyArray = Array<Int>

            fun test(a: MyArray) = a.size >= 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a size check inside an isNotEmpty function passes`() {
        val code = """
            class EmptyCollection<T> : AbstractCollection<T>() {
                override val size: Int
                    get() = 0

                override fun iterator(): Iterator<T> = emptyList<T>().iterator()

                fun isNotEmpty(): Boolean = size != 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a size check inside an isNotEmpty extension passes`() {
        val code = """
            class EmptyCollection<T> : AbstractCollection<T>() {
                override val size: Int
                    get() = 0

                override fun iterator(): Iterator<T> = emptyList<T>().iterator()
            }

            fun <T> EmptyCollection<T>.isNotEmpty(): Boolean = size != 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a count with a predicate passes`() {
        val code = """
            fun test(items: Iterable<Int>, list: List<Int>) = items.count { it > 0 } != 0 && list.count { it == 2 } != 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an iterable count passes`() {
        val code = """
            fun test(items: Iterable<Int>) = items.count() > 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe size call passes`() {
        val code = """
            fun m(s: List<String>?) = s?.size != 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a size of a custom class passes`() {
        val code = """
            class List {
                val size = 0
            }

            fun test() = List().size >= 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a size compared with another number passes`() {
        val code = """
            fun test(items: List<Int>) = items.size > 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
