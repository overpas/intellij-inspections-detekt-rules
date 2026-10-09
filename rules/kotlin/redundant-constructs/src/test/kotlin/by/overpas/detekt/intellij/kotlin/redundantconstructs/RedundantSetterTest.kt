package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantSetterTest {

    private val environment = createEnvironment()

    private val sut = RedundantSetter(Config.empty)

    @Test
    fun `a default setter is reported`() {
        val code = """
            class Test {
                var x = 1
                    set
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter that only assigns the backing field is reported`() {
        val code = """
            class Test {
                var x = 1
                    set(value) {
                        field = value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter with the same visibility as the property is reported`() {
        val code = """
            class Test {
                internal var x = 1
                    internal set
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an annotated setter with a trivial body is reported`() {
        val code = """
            class Foo {
                var foo: String = ""
                    @Deprecated("") set(x) {
                        field = x
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a default setter of an overriding property is reported`() {
        val code = """
            interface A {
                var myVar: Boolean
            }

            class X : A {
                override var myVar: Boolean = false
                    set
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a trivial setter body that raises the access of an overridden setter is reported`() {
        val code = """
            open class Base {
                open var foo: String = ""
                    protected set
            }

            class Bar : Base() {
                override var foo: String = ""
                    public set(value) {
                        field = value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter without a body that raises the access of an overridden setter passes`() {
        val code = """
            open class Base {
                open var foo: String = ""
                    protected set
            }

            class Bar : Base() {
                override var foo: String = ""
                    public set
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an annotated setter without a body passes`() {
        val code = """
            annotation class Inject

            class Test {
                var x = 1
                    @Inject set
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter with a lower visibility passes`() {
        val code = """
            class Test {
                var x = 1
                    private set
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter with a blank body passes`() {
        val code = """
            class Test {
                var x = 1
                    set(value) {
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter with a comment passes`() {
        val code = """
            class Test {
                var x = 1
                    set(value) {
                        // comment
                        field = value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter that does more than assign the backing field passes`() {
        val code = """
            class Test {
                var x = 1
                    set(value) {
                        foo()
                        field = value
                    }

                fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
