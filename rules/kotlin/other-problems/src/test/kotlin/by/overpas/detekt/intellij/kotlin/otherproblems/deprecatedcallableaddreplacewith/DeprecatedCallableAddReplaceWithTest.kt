package by.overpas.detekt.intellij.kotlin.otherproblems.deprecatedcallableaddreplacewith

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class DeprecatedCallableAddReplaceWithTest {

    private val environment = createEnvironment()

    private val sut = DeprecatedCallableAddReplaceWith(Config.empty)

    @Test
    fun `a deprecated function with a single call is reported`() {
        val code = """
            @Deprecated("")
            fun foo() {
                bar()
            }

            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated function with an expression body is reported`() {
        val code = """
            @Deprecated("")
            fun foo(): String = bar()

            fun bar(): String = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated function with a single return is reported`() {
        val code = """
            @Deprecated("")
            fun foo(): String {
                return bar()
            }

            fun bar(): String = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated function with an if statement is reported`() {
        val code = """
            @Deprecated("")
            fun foo(p: Int) {
                if (p > 0)
                    bar1(p)
                else
                    bar2(p)
            }

            fun bar1(p: Int) {}
            fun bar2(p: Int) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated function with a comment and a qualified call is reported`() {
        val code = $$"""
            @Deprecated(message = "")
            fun foo(s: String, p: Int) {
                // invoke bar()
                s.bar("$p ${p + 1}")
            }

            fun String.bar(s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `deprecated property getters are reported`() {
        val code = """
            class C {
                @Deprecated("")
                val foo: String
                    get() = bar()

                @Deprecated("")
                val baz: String
                    get() {
                        return bar()
                    }

                fun bar(): String = ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a deprecated operator function that is not a delegate convention is reported`() {
        val code = """
            class Box(val value: Int)

            @Deprecated("")
            operator fun Box.plus(other: Box): Box = Box(value + other.value)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated function named getValue without the operator modifier is reported`() {
        val code = """
            class Box(val value: Int)

            @Deprecated("")
            fun Box.getValue(): Int = value
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated getValue operator passes`() {
        val code = """
            import kotlin.reflect.KProperty

            class Box(val value: Int)

            @Deprecated("")
            operator fun Box.getValue(thisRef: Any?, property: KProperty<*>): Int = value
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated setValue operator passes`() {
        val code = """
            import kotlin.reflect.KProperty

            class Box(var value: Int)

            @Deprecated("")
            operator fun Box.setValue(thisRef: Any?, property: KProperty<*>, newValue: Int) {
                update(newValue)
            }

            fun Box.update(newValue: Int) {
                value = newValue
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated provideDelegate operator passes`() {
        val code = """
            import kotlin.reflect.KProperty

            class Box(val value: Int)

            @Deprecated("")
            operator fun Box.provideDelegate(thisRef: Any?, property: KProperty<*>): Box = Box(value)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated function with a replaceWith argument passes`() {
        val code = """
            @Deprecated("", ReplaceWith("bar()"))
            fun foo() {
                bar()
            }

            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a hidden deprecated function passes`() {
        val code = """
            @Deprecated("Use the other version", level = DeprecationLevel.HIDDEN)
            fun foo(a: Int) {
                foo(a, 0)
            }

            fun foo(a: Int, b: Int) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated function with two statements passes`() {
        val code = """
            @Deprecated("")
            fun foo() {
                bar()
                bar()
            }

            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated function with a declaration inside passes`() {
        val code = """
            @Deprecated("")
            fun foo(p: Int) {
                if (p > 0) {
                    val v = p + 1
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated function with a nested return passes`() {
        val code = """
            @Deprecated("")
            fun foo() {
                bar() ?: return
            }

            fun bar(): String? = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated function that uses a private member passes`() {
        val code = """
            class C {
                private val v = 1

                @Deprecated("")
                fun foo() {
                    bar(v)
                }

                fun bar(p: Int) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated function with a non-return statement and a non-Unit type passes`() {
        val code = """
            @Deprecated("")
            fun foo(): String {
                throw IllegalStateException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deprecated var property passes`() {
        val code = """
            class C {
                @Deprecated("")
                var foo: String = ""
                    get() = field
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function without a deprecated annotation passes`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
