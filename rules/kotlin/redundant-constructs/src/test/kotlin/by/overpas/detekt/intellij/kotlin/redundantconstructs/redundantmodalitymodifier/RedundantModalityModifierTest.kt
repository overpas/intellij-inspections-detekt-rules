package by.overpas.detekt.intellij.kotlin.redundantconstructs.redundantmodalitymodifier

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantModalityModifierTest {

    private val environment = createEnvironment()

    private val sut = RedundantModalityModifier(Config.empty)

    @Test
    fun `a final class is reported`() {
        val code = """
            final class Final
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final object is reported`() {
        val code = """
            final object FinalObject
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an abstract interface passes`() {
        val code = """
            abstract interface AbstractInterface
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open interface is reported`() {
        val code = """
            open interface OpenInterface
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an abstract function in an interface passes`() {
        val code = """
            interface Interface {
                abstract fun foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an abstract property in an interface passes`() {
        val code = """
            interface Interface {
                abstract val gav: Int
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open function with a body in an interface is reported`() {
        val code = """
            interface Interface {
                open fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an open property with a getter in an interface is reported`() {
        val code = """
            interface Interface {
                open val gav: Int
                    get() = 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final function in an abstract class is reported`() {
        val code = """
            abstract class Base {
                final fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final override in a final class is reported`() {
        val code = """
            abstract class Base {
                abstract fun bar()
            }

            class FinalDerived : Base() {
                override final fun bar() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an open override in an open class is reported`() {
        val code = """
            abstract class Base {
                open val gav = 42
            }

            open class OpenDerived : Base() {
                override open val gav = 13
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an open override in a derived interface is reported`() {
        val code = """
            interface Interface {
                fun foo()
            }

            interface Derived : Interface {
                override open fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an abstract function in an abstract class passes`() {
        val code = """
            abstract class Base {
                abstract fun foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open property in an abstract class passes`() {
        val code = """
            abstract class Base {
                open val gav = 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a final override in an open class passes`() {
        val code = """
            abstract class Base {
                abstract fun bar()
            }

            open class OpenDerived : Base() {
                override final fun bar() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a final override property in an abstract class constructor passes`() {
        val code = """
            interface Interface {
                val gav: Int
                    get() = 42
            }

            abstract class AbstractDerived(override final val gav: Int) : Interface
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a final class with an all-open annotation passes`() {
        val sut = RedundantModalityModifier(TestConfig("allOpenAnnotations" to listOf("spring.Component")))
        val code = """
            package spring

            annotation class Component

            @Component
            final class Repository
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a final function in a class with an all-open meta-annotation passes`() {
        val sut = RedundantModalityModifier(TestConfig("allOpenAnnotations" to listOf("spring.Component")))
        val code = """
            package spring

            annotation class Component

            @Component
            annotation class Service

            @Service
            class Repository {
                final fun find() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a final class without an all-open annotation is reported`() {
        val sut = RedundantModalityModifier(TestConfig("allOpenAnnotations" to listOf("spring.Component")))
        val code = """
            package spring

            annotation class Entity

            @Entity
            final class Repository
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }
}
