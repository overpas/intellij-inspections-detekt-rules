package by.overpas.detekt.intellij.kotlin.styleissues.addoperatormodifier

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class AddOperatorModifierTest {

    private val environment = createEnvironment()

    private val sut = AddOperatorModifier(Config.empty)

    @Test
    fun `a member plus function without the operator modifier is reported`() {
        val code = """
            class A {
                fun plus(other: A): A = A()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains function that returns Boolean is reported`() {
        val code = """
            class A {
                fun contains(other: A): Boolean = true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension plus function is reported`() {
        val code = """
            class A

            fun A.plus(other: A): A = A()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override of a plain plus function is reported`() {
        val code = """
            open class A {
                open fun plus(other: A): A = A()
            }

            class B : A() {
                override fun plus(other: A): A = super.plus(other)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a member get function is reported`() {
        val code = """
            class Grid {
                fun get(row: Int, column: Int): Int = row * column
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains function that returns Int passes`() {
        val code = """
            class A {
                fun contains(other: A): Int = -1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override of an operator function passes`() {
        val code = """
            open class A {
                open operator fun plus(a: A) = A()
            }

            class B : A() {
                override fun plus(a: A) = B()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a top-level plus function without a receiver passes`() {
        val code = """
            class A

            fun plus(other: A): A = A()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function with a name that is not an operator convention passes`() {
        val code = """
            class A {
                fun combine(other: A): A = A()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a suspend get function passes by default`() {
        val code = """
            interface NotesDb {
                suspend fun get(id: Long): String?
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a suspend get function passes when suspend functions are skipped`() {
        val sut = AddOperatorModifier(TestConfig("areSuspendFunctionsSkipped" to true))
        val code = """
            interface NotesDb {
                suspend fun get(id: Long): String?
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a suspend get function is reported when suspend functions are not skipped`() {
        val sut = AddOperatorModifier(TestConfig("areSuspendFunctionsSkipped" to false))
        val code = """
            interface NotesDb {
                suspend fun get(id: Long): String?
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member set function that returns Unit is reported`() {
        val code = """
            class Settings {
                fun set(key: String, value: Int) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member set function that returns a value passes`() {
        val code = """
            class Job

            class Settings {
                fun set(key: String, value: Int): Job = Job()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension set function with an inferred non-Unit return type passes`() {
        val code = """
            class Settings

            fun <T> Settings.set(key: String, value: T) = listOf(key, value)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member get function that returns a value is still reported`() {
        val code = """
            class Settings {
                fun get(key: String): Int = 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a non-suspend get function is reported when suspend functions are skipped`() {
        val sut = AddOperatorModifier(TestConfig("areSuspendFunctionsSkipped" to true))
        val code = """
            interface NotesDb {
                fun get(id: Long): String?
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }
}
