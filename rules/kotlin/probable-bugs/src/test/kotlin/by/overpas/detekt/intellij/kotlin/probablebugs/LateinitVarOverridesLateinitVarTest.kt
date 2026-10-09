package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class LateinitVarOverridesLateinitVarTest {

    private val environment = createEnvironment()

    private val sut = LateinitVarOverridesLateinitVar(Config.empty)

    @Test
    fun `a lateinit var that overrides a lateinit var is reported`() {
        val code = """
            open class A1 {
                open lateinit var a: String
            }

            class A2 : A1() {
                override lateinit var a: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lateinit var that overrides a lateinit var of a grandparent is reported`() {
        val code = """
            open class A1 {
                open lateinit var a: String
            }

            open class A2 : A1()

            class A3 : A2() {
                override lateinit var a: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lateinit var that overrides a plain var passes`() {
        val code = """
            open class A1 {
                open var a: String = ""
            }

            class A2 : A1() {
                override lateinit var a: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plain var that overrides a lateinit var passes`() {
        val code = """
            open class A1 {
                open lateinit var a: String
            }

            class A2 : A1() {
                override var a: String = ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lateinit var that overrides an abstract var passes`() {
        val code = """
            abstract class A1 {
                abstract var a: String
            }

            class A2 : A1() {
                override lateinit var a: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lateinit var that overrides an interface var passes`() {
        val code = """
            interface A1 {
                var a: String
            }

            class A2 : A1 {
                override lateinit var a: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
