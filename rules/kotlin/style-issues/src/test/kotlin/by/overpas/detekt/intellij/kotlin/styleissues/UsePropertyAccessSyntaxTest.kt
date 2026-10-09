package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class UsePropertyAccessSyntaxTest {

    private val environment = createEnvironment(listOf(Path("src/test/resources/UsePropertyAccessSyntax")))

    private val sut = UsePropertyAccessSyntax(Config.empty)

    @Test
    fun `a Java getter call is reported`() {
        val code = """
            fun main() {
                val j = J()
                j.getX()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Java setter call is reported`() {
        val code = """
            fun main() {
                val j = J()
                j.setX(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Java is-getter call is reported`() {
        val code = """
            fun main() {
                Switch().isActive()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Java is-getter call with a backticked name is reported`() {
        val code = """
            fun main() {
                Switch().`isActive`()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter at the start of a call chain is reported`() {
        val code = """
            fun main() {
                J().getX().doSth()
            }

            fun Int.doSth() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter at the end of a call chain is reported`() {
        val code = """
            fun main() {
                J().getThis().getX()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter in a function that returns Unit is reported`() {
        val code = """
            fun setButReturnUnit(x: Int) {
                J().setX(x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter of a Java interface implemented in Kotlin is reported`() {
        val code = """
            class Named : JavaName {
                override fun getName(): String {
                    return "name"
                }
            }

            fun main() {
                Named().getName()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a JDK setter call is reported`() {
        val code = """
            fun foo(thread: Thread) {
                thread.setName("name")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a JDK getter call after a safe call is reported`() {
        val code = """
            fun foo(thread: Thread?) {
                thread?.getName()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter of a Kotlin class passes`() {
        val code = """
            class K {
                private var x: Int = 0

                fun getX(): Int {
                    return x
                }
            }

            fun main() {
                K().getX()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with a reserved word as property name passes`() {
        val code = """
            fun test() {
                Keywords().getObject()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a non-trivial Java setter passes`() {
        val code = """
            fun test() {
                Computed().setValue(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a non-trivial Java getter passes`() {
        val code = """
            fun test() {
                Computed().getX()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a method that only looks like an accessor passes`() {
        val code = """
            fun test() {
                Computed().canGetX()
                Computed().willSetX(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an is-method that does not return Boolean passes`() {
        val code = """
            fun test() {
                Switch().isTrue()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter of a class with a public field of the same name passes`() {
        val code = """
            fun test() {
                Exposed().getName()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter at the start of a call chain passes`() {
        val code = """
            fun main() {
                J().setX(1).doSth()
            }

            fun Int.doSth() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter whose result is used passes`() {
        val code = """
            fun main() {
                val a = J().setX(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter after return passes`() {
        val code = """
            fun setValue(x: Int): Int {
                return J().setX(x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter with a lambda argument passes`() {
        val code = """
            fun test() {
                Runner().setR { println("Hello") }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter called on super passes`() {
        val code = """
            open class Base : JavaName {
                override fun getName(): String = ""
            }

            class Child : Base() {
                override fun getName(): String = super.getName()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter listed as not a property passes`() {
        val code = """
            import java.util.concurrent.atomic.AtomicLong

            fun main() {
                AtomicLong().getAndIncrement()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an isEmpty call on a Java collection passes`() {
        val code = """
            fun test() {
                java.util.HashSet<Int>().isEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
