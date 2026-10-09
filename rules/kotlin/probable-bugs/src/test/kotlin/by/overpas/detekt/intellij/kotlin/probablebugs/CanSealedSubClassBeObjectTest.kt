package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CanSealedSubClassBeObjectTest {

    private val environment = createEnvironment()

    private val sut = CanSealedSubClassBeObject(Config.empty)

    @Test
    fun `a stateless subclass of a sealed class is reported`() {
        val code = """
            sealed class Sealed

            class SubSealed : Sealed()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a stateless subclass with empty parentheses is reported`() {
        val code = """
            sealed class Sealed

            class SubSealed() : Sealed()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a private stateless subclass is reported`() {
        val code = """
            sealed class Sealed

            private class SubSealed : Sealed()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a stateless subclass with a parameterless secondary constructor is reported`() {
        val code = """
            sealed class Sealed

            class SubSealed : Sealed {
                constructor() {
                    println("init")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a stateless implementation of a sealed interface is reported`() {
        val code = """
            sealed interface Sealed

            class SubSealed : Sealed
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a subclass with properties that only have accessors is reported`() {
        val code = """
            abstract class Base {
                var s: String
                    get() = "Hello"
                    set(value) {}
            }

            sealed class Sealed : Base() {
                open val x: List<Int>
                    get() = emptyList()
            }

            class Derived : Sealed() {
                var length: Int
                    get() = s.length
                    set(value) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a nested stateless subclass is reported`() {
        val code = """
            abstract class Base {
                open val prop: Int
                    get() = 13
            }

            sealed class SC : Base() {
                class U : SC()

                override val prop: Int
                    get() = 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a subclass of a sealed class whose base overrides equals passes`() {
        val code = """
            abstract class Base {
                open val prop: Int
                    get() = 13

                override fun equals(other: Any?): Boolean = other is Base && prop == other.prop
            }

            sealed class SC : Base() {
                class U : SC()

                override val prop: Int
                    get() = 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass of a sealed class whose base has state passes`() {
        val code = """
            abstract class Base(var x: String)

            sealed class Sealed(s: String) : Base(s)

            class Derived : Sealed("123")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a generic subclass passes`() {
        val code = """
            sealed class Sealed<T>

            class SubSealed<T> : Sealed<T>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass with constructor parameters passes`() {
        val code = """
            sealed class Sealed(val y: Int)

            class SubSealed(x: Int) : Sealed(x)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open subclass passes`() {
        val code = """
            sealed class Sealed

            open class SubSealed : Sealed()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed subclass passes`() {
        val code = """
            sealed class Bar

            sealed class Foo : Bar()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass with own equals passes`() {
        val code = """
            sealed class SC {
                class U : SC() {
                    override fun equals(other: Any?): Boolean = this === other
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass with own state passes`() {
        val code = """
            sealed class SC {
                class U : SC() {
                    val a = mutableListOf<String>()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass of a sealed class with equals passes`() {
        val code = """
            sealed class SC {
                class U : SC()

                fun foo() = 42

                override fun equals(other: Any?): Boolean = other is SC && foo() == other.foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass of a sealed class with state passes`() {
        val code = """
            sealed class SC {
                var u = 0

                class C : SC()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass with a companion object passes`() {
        val code = """
            sealed class Sealed

            class SubSealed : Sealed() {
                companion object
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass with an inner class passes`() {
        val code = """
            sealed class Sealed

            class SubSealed : Sealed() {
                inner class Inner
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
