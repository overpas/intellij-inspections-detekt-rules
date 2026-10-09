package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveRedundantQualifierNameTest {

    private val environment = createEnvironment()

    private val sut = RemoveRedundantQualifierName(Config.empty)

    @Test
    fun `a package qualifier of a type is reported`() {
        val code = """
            package my.simple.name

            class Foo

            fun foo(a: my.simple.name.Foo) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a package qualifier of a constructor call is reported`() {
        val code = """
            package my.simple.name

            class Foo

            fun main() {
                val c = my.simple.name.Foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a package qualifier of a top level property is reported`() {
        val code = """
            package my.simple.name

            val foo = 3

            class A {
                fun a() = my.simple.name.foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of an imported companion member is reported`() {
        val code = """
            import Foo.Companion.foo

            class Foo {
                companion object {
                    fun foo() = ""
                }
            }

            fun test() = Foo.Companion.foo()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of an imported enum entry is reported`() {
        val code = """
            import Encoding.MJPEG

            class Player {
                val status: String = Encoding.MJPEG.toString()
            }

            enum class Encoding { UNKNOWN, MJPEG }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of a nested class from a supertype is reported`() {
        val code = """
            open class MyBaseClass {
                class Nested
            }

            class Foo : MyBaseClass() {
                fun test(p: MyBaseClass.Nested) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a default import qualifier is reported`() {
        val code = """
            fun foo(s: kotlin.String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of an enum entry that is not imported passes`() {
        val code = """
            class Player {
                val status: String = Encoding.MJPEG.toString()
            }

            enum class Encoding { UNKNOWN, MJPEG }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualifier of an object member passes`() {
        val code = """
            class Foo {
                val prop = Obj.prop.toString()
            }

            object Obj {
                val prop = "Hello"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualifier of an enum companion member passes`() {
        val code = """
            package foo.bar

            import foo.bar.MyEnum.*

            enum class MyEnum(val id: Int) {
                A(1),
                B(2);

                companion object {
                    fun baz() = ""
                }
            }

            fun test() {
                MyEnum.Companion::baz
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an outer class qualifier of a nested type is reported`() {
        val code = """
            package my.simple.name

            typealias Int = Long

            class Outer {
                class Middle {
                    class Int
                    class Inner {
                        fun goo(i: Outer.Middle.Int) {}
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier needed to avoid a local variable clash passes`() {
        val code = """
            package my.simple.name

            class Inner {
                fun a() {
                    val MAX = 2
                    val a = Member.MAX
                }

                companion object Member {
                    val MAX = 1
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a package qualifier needed to avoid a nested class clash passes`() {
        val code = """
            package my.simple.name

            open class SuperClass {
                companion object {
                    fun check() {}
                }
            }

            class Foo

            class Child : SuperClass() {
                class Foo constructor() {
                    constructor(i: Int) : this()

                    class SuperClass

                    fun check() {}

                    fun foo() {
                        my.simple.name.SuperClass.check()
                    }

                    companion object {
                        fun check() {}
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unqualified name passes`() {
        val code = """
            class Foo

            fun foo(a: Foo) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
