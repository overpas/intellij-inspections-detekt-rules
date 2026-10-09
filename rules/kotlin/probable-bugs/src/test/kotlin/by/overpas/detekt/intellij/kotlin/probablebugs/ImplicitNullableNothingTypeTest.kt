package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ImplicitNullableNothingTypeTest {

    private val environment = createEnvironment()

    private val sut = ImplicitNullableNothingType(Config.empty)

    @Test
    fun `a top-level var initialized with null is reported`() {
        val code = """
            var x = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an open function returning null is reported`() {
        val code = """
            open class My {
                open fun foo() = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an open val initialized with null is reported`() {
        val code = """
            open class My {
                open val foo = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an overriding var initialized with null is reported`() {
        val code = """
            abstract class Parent {
                abstract val foo: Int?
            }

            class Child : Parent() {
                override var foo = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final function returning null passes`() {
        val code = """
            class My {
                fun foo() = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a top-level function returning null passes`() {
        val code = """
            fun foo() = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a val initialized with null passes`() {
        val code = """
            val x = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var with an explicit type passes`() {
        val code = """
            var x: Nothing? = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an overriding function returning null passes`() {
        val code = """
            abstract class Parent {
                protected abstract fun foo(): String?
            }

            class Child : Parent() {
                override fun foo() = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an overriding var of a Nothing type property passes`() {
        val code = """
            abstract class Parent {
                abstract val foo: Nothing?
            }

            class Child : Parent() {
                override var foo = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an overriding val initialized with null passes`() {
        val code = """
            abstract class Parent {
                abstract val foo: Int?
            }

            class Child : Parent() {
                override val foo = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
