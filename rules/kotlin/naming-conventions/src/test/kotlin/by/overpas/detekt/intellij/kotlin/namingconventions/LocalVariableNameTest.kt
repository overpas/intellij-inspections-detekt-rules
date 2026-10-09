package by.overpas.detekt.intellij.kotlin.namingconventions

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalVariableNameTest {

    private val environment = createEnvironment()

    private val sut = LocalVariableName(Config.empty)

    @Test
    fun `a local variable with an underscore is reported`() {
        val code = """
            fun foo() {
                val local_val = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a local variable with a space in backticks is reported`() {
        val code = """
            fun foo() {
                val `a b` = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a destructured variable with an underscore is reported`() {
        val code = """
            fun foo() {
                val (destructured_val, _) = listOf(1, 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function parameter with an underscore is reported`() {
        val code = """
            fun foo(fun_param: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `constructor parameters without val or var with bad names are reported`() {
        val code = """
            class C(ctor_param: String, CtorParam: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a lambda parameter that starts with an uppercase letter is reported`() {
        val code = """
            fun foo() {
                "".let { Value -> Value.length }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a loop variable with an underscore is reported`() {
        val code = """
            fun foo() {
                for (loop_item in listOf(1, 2)) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a catch parameter that starts with an uppercase letter is reported`() {
        val code = """
            fun foo() {
                try {
                } catch (Error: Exception) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `constructor properties and member properties pass`() {
        val code = """
            interface Parameter {
                val interface_p1: String
                val interface_p2: String
            }

            class ParameterImpl(
                override val interface_p1: String,
                private val ctor_private: String,
                val ctor_val: String,
                var ctor_var: String,
            ) : Parameter {
                override val interface_p2: String = ""
                val Member_Property = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `underscore names pass`() {
        val code = """
            var setter
                get() = 42
                set(_) {}

            fun foo() {
                try {
                } catch (_: Exception) {
                }
                "".let { _: String -> }
                "".let(fun(_: String) {})
                val (_, second) = listOf(1, 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `lower camel case names pass`() {
        val code = """
            fun foo(firstParam: String, second2: Int) {
                val localValue = firstParam.length + second2
                "".let { item -> item.length + localValue }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
