package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceCollectionCountWithSizeTest {

    private val environment = createEnvironment()

    private val sut = ReplaceCollectionCountWithSize(Config.empty)

    @Test
    fun `a count call on a list is reported`() {
        val code = """
            fun foo() {
                val list = listOf(1, 2, 3)
                list.count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a count call on an array is reported`() {
        val code = """
            fun foo() {
                val array = arrayOf(1, 2, 3)
                array.count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a count call on a primitive array is reported`() {
        val code = """
            fun foo() {
                val array = longArrayOf(1, 2, 3)
                array.count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a count call on a map is reported`() {
        val code = """
            fun foo() {
                val map = mapOf(1 to true)
                map.count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a count call with an implicit collection receiver is reported`() {
        val code = """
            fun foo() {
                val list = listOf(1, 2, 3)
                list.run {
                    count()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a count call with a predicate passes`() {
        val code = """
            fun foo() {
                val array = arrayOf(1, 2, 3)
                array.count { i -> i == 1 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a count call on an iterable passes`() {
        val code = """
            fun foo(iterable: Iterable<String>) {
                iterable.count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe count call on a nullable iterable passes`() {
        val code = """
            fun foo(iterable: Iterable<String>?) {
                iterable?.count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a count call on an iterable subclass passes`() {
        val code = """
            class Foo : Iterable<Int> {
                override fun iterator(): Iterator<Int> = listOf(1).iterator()
            }

            fun foo() {
                Foo().count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a count member function passes`() {
        val code = """
            class Foo {
                fun count() = 0
            }

            fun foo() {
                Foo().count()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
