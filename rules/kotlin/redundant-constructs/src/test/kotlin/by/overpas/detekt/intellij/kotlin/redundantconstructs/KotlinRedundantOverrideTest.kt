package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinRedundantOverrideTest {

    private val environment = createEnvironment()

    private val sut = KotlinRedundantOverride(Config.empty)

    @Test
    fun `an override that only calls super is reported`() {
        val code = """
            open class Foo {
                open fun simple() {}
            }

            class Bar : Foo() {
                override fun simple() {
                    super.simple()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override that passes its arguments to super is reported`() {
        val code = """
            open class Foo {
                open fun arguments(arg1: Int, arg2: Long) {}
            }

            class Bar : Foo() {
                override fun arguments(arg1: Int, arg2: Long) {
                    super.arguments(arg1, arg2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override of a class member that also implements an interface member is reported`() {
        val code = """
            open class Class {
                open fun foo(): Int = 4
            }

            interface Interface {
                fun foo(): Int
            }

            class ChildClass : Class(), Interface {
                override fun foo() = super.foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override in a data class of a non-Any member is reported`() {
        val code = """
            open class Foo {
                open fun foo() = 1
            }

            data class D(val i: Int) : Foo() {
                override fun foo(): Int = super.foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override of equals that calls the implementation of Any is reported`() {
        val code = """
            class Test {
                override fun equals(other: Any?) = super.equals(other)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override with a property of another type is reported`() {
        val code = """
            open class A {
                open fun isFoo(): Boolean = true
            }

            class B : A() {
                private val isFoo: Int = 42

                override fun isFoo(): Boolean = super.isFoo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an override with swapped arguments passes`() {
        val code = """
            open class Foo {
                open fun arguments(arg1: Int, arg2: Int) {}
            }

            class Bar : Foo() {
                override fun arguments(arg1: Int, arg2: Int) {
                    super.arguments(arg2, arg1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an annotated override passes`() {
        val code = """
            @Target(AnnotationTarget.FUNCTION)
            annotation class Marker

            open class Foo {
                open fun simple() {}
            }

            class Bar : Foo() {
                @Marker
                override fun simple() {
                    super.simple()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override with another modifier passes`() {
        val code = """
            open class Foo {
                protected open fun simple() {}
            }

            class Bar : Foo() {
                public override fun simple() {
                    super.simple()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override with a qualified super call passes`() {
        val code = """
            interface First {
                fun foo() = 2
            }

            interface Second {
                fun foo() = 3
            }

            class Diamond : First, Second {
                override fun foo(): Int {
                    return super<First>.foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override that picks an implementation among several passes`() {
        val code = """
            abstract class AbstractClass {
                abstract fun foo(): Int
            }

            interface Interface {
                fun foo(): Int = 3
            }

            class ChildClass : AbstractClass(), Interface {
                override fun foo() = super.foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override of toString in a data class passes`() {
        val code = """
            data class My(val x: Int, val y: String) {
                override fun toString(): String = super.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override of an abstract Any member from an interface passes`() {
        val code = """
            interface I {
                override fun equals(other: Any?): Boolean
            }

            class Test : I {
                override fun equals(other: Any?) = super.equals(other)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override of a getter with a property of the same name passes`() {
        val code = """
            open class A {
                open fun getFoo(): String? = null
            }

            class B : A() {
                private val foo = ""

                override fun getFoo(): String? = super.getFoo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override of a member of an interface implemented by delegation passes`() {
        val code = """
            interface Foo {
                fun foo()
            }

            open class C1 : Foo {
                override fun foo() {}
            }

            class C2(delegate: Foo) : C1(), Foo by delegate {
                override fun foo() {
                    super.foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override with a boxed parameter of a Java superclass passes`() {
        val code = """
            interface Foo {
                fun add(i: Int): Boolean
            }

            class Bar : java.util.ArrayList<Int>(), Foo {
                override fun add(i: Int) = super.add(i)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
