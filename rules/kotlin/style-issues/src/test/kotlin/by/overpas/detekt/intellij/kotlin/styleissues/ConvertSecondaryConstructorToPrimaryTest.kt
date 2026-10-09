package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertSecondaryConstructorToPrimaryTest {

    private val environment = createEnvironment()

    private val sut = ConvertSecondaryConstructorToPrimary(Config.empty)

    @Test
    fun `a single empty secondary constructor is reported`() {
        val code = """
            class Simple {
                constructor()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a secondary constructor that initializes properties is reported`() {
        val code = """
            class DefaultValueChain {
                val x1: Int
                val x3: Int

                constructor(x1: Int, x2: Int = x1, x3: Int = x2) {
                    this.x1 = x1
                    this.x3 = x3
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a secondary constructor that calls the super constructor is reported`() {
        val code = """
            abstract class Base(val x: String)

            class Derived : Base {
                constructor(x: String) : super(x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a protected secondary constructor with a body is reported`() {
        val code = """
            open class Protected {
                internal var s: String

                protected constructor(s: String) {
                    this.s = s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a secondary constructor that the others delegate to is reported`() {
        val code = """
            class Chain {
                constructor(x: String)

                constructor(x: Int) : this(x.toString())

                constructor(x: Int, y: Int) : this(x + y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a secondary constructor with a vararg parameter is reported`() {
        val code = """
            class WithVarArg {
                val x: List<String>

                constructor(vararg zz: String) {
                    x = listOf(*zz)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a secondary constructor that another constructor does not reach passes`() {
        val code = """
            class NonReachableConstructor {
                constructor(x: String)

                constructor(x: Int)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `only the target of a delegation to this is reported`() {
        val code = """
            class WithDelegation {
                constructor(x: Int, y: Int)

                constructor(x: Int) : this(x, x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a secondary constructor in a class with a primary constructor passes`() {
        val code = """
            class WithPrimary(val x: Int) {
                constructor(x: String) : this(x.length)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
