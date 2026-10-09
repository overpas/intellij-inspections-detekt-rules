package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceWithImportAliasTest {

    private val environment = createEnvironment()

    private val sut = ReplaceWithImportAlias(Config.empty)

    @Test
    fun `a qualified constructor call of an aliased class is reported`() {
        val code = """
            package foo

            import foo.Foo as Bar

            class Foo

            fun main() {
                foo.Foo()
                foo.Foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a qualified receiver type of an aliased class is reported`() {
        val code = """
            package foo

            import foo.Foo as Bar

            class Foo

            fun foo.Foo.test() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified return type of an aliased class is reported`() {
        val code = """
            package foo

            import foo.Foo as Bar

            class Foo

            fun test(): foo.Foo? = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified call of an aliased function is reported`() {
        val code = """
            package foo

            import foo.foo as bar

            fun foo() {}

            fun main() {
                foo.foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified reference to an aliased property is reported`() {
        val code = """
            package foo

            import foo.foo as bar

            val foo = 1

            fun main() {
                foo.foo
                foo.foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a qualified name with several aliases is reported`() {
        val code = """
            package foo

            import foo.Foo as Bar
            import foo.Foo as Baz

            class Foo

            fun main() {
                foo.Foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an import directive passes`() {
        val code = """
            package foo

            import foo.Foo as Bar

            class Foo
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a usage of the alias passes`() {
        val code = """
            package foo

            import foo.Foo as Bar

            class Foo

            fun main() {
                Bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unqualified type passes`() {
        val code = """
            package foo

            import foo.Foo as Bar

            class Foo

            val foo: Foo? = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified name of another declaration with the same short name passes`() {
        val code = """
            package foo

            import bar.Foo as Baz

            class Foo

            fun main() {
                foo.Foo()
                Baz()
            }
        """.trimIndent()
        val dependency = """
            package bar

            class Foo
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified name without an alias import passes`() {
        val code = """
            package foo

            class Foo

            fun main() {
                foo.Foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
