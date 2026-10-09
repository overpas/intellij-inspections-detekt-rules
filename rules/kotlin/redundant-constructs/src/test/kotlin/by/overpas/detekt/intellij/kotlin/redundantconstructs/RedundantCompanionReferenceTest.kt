package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantCompanionReferenceTest {

    private val environment = createEnvironment()

    private val sut = RedundantCompanionReference(Config.empty)

    @Test
    fun `a qualified companion reference is reported`() {
        val code = """
            class C {
                companion object {
                    fun create() = C()
                }
            }

            fun test() {
                C.Companion.create()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a direct companion reference is reported`() {
        val code = """
            class C {
                companion object {
                    fun create() = C()
                }

                fun test() {
                    Companion.create()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to a named companion is reported`() {
        val code = """
            class C {
                companion object Obj {
                    fun create() = C()
                }
            }

            fun test() {
                C.Obj.create()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a companion reference in a class named Companion is reported`() {
        val code = """
            class Companion {
                fun test() {
                    Companion.foo
                }

                companion object {
                    val foo = ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a companion reference inside an enum is reported`() {
        val code = """
            enum class E {
                E1;

                fun test() {
                    bar(Companion.foo)
                }

                fun bar(s: String) {}

                companion object {
                    const val foo = ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a companion reference to a function with other parameter types than a member is reported`() {
        val code = """
            class Test {
                companion object {
                    fun f(x: Int, y: Int) = 1
                }

                fun f(x: Int, y: String) = 2

                fun test() {
                    Companion.f(1, 2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to a class passes`() {
        val code = """
            class C {
                companion object {
                    fun create() = C()
                }
            }

            fun test() {
                C.create()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference to a nested class of the companion passes`() {
        val code = """
            class Owner {
                companion object {
                    class InCompanion
                }
            }

            val y = Owner.Companion.InCompanion()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference without a selector passes`() {
        val code = """
            class C {
                companion object {
                    fun create() = C()
                }
            }

            fun test() {
                C.Companion
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference in an import passes`() {
        val code = """
            import Owner.Companion.some

            class Owner {
                companion object {
                    const val some = ""
                }
            }

            class User {
                val anything = some
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference to values of an enum passes`() {
        val code = """
            enum class A {
                TEST;

                companion object {
                    fun values() {}
                }

                fun test() {
                    Companion.values()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference shadowed by a member property passes`() {
        val code = """
            class Test {
                companion object {
                    val memberVar = 1
                }

                val memberVar = 2

                fun test() {
                    Companion.memberVar
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference shadowed by a local variable passes`() {
        val code = """
            class Test {
                companion object {
                    val localVar = 1
                }

                fun test(localVar: Int) {
                    Companion.localVar
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference shadowed by an extension property passes`() {
        val code = """
            class Test {
                companion object {
                    val extensionVar = 1
                }

                fun test() {
                    Companion.extensionVar
                }
            }

            val Test.extensionVar: Int
                get() = 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference shadowed by a super class member passes`() {
        val code = $$"""
            open class B {
                fun foo(x: Int) = "B$x"
            }

            class C : B() {
                fun test(): String {
                    return Companion.foo(1)
                }

                companion object {
                    fun foo(x: Int) = "C$x"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference shadowed by an implicit receiver passes`() {
        val code = """
            class A {
                val foo = "string"
            }

            class B {
                companion object {
                    val foo = 2

                    fun A.bar(): Int = Companion.foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion reference with the same name as its member passes`() {
        val code = """
            class A {
                companion object foo {
                    val foo = 1
                }

                fun test() {
                    foo.foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
