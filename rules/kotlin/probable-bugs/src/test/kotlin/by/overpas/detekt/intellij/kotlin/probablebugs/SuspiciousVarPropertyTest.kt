package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousVarPropertyTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousVarProperty(Config.empty)

    @Test
    fun `a var whose getter does not read the backing field is reported`() {
        val code = """
            class Test {
                var foo: Int = 0
                    get() = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a var overriding a val whose getter does not read the backing field is reported`() {
        val code = """
            open class Base {
                open val foo: Int = 0
            }

            class Child : Base() {
                override var foo: Int = 0
                    get() = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a top-level var whose block getter does not read the backing field is reported`() {
        val code = """
            var foo: Int = 0
                get() {
                    return 1
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a var whose getter returns the backing field passes`() {
        val code = """
            class Test {
                var foo: Int = 0
                    get() = field
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var whose block getter returns the backing field passes`() {
        val code = """
            class Test {
                var foo: Int = 0
                    get() {
                        return field
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var whose getter uses the backing field in an expression passes`() {
        val code = """
            class Test {
                var foo: Int = 0
                    get() = field + 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var with a default getter passes`() {
        val code = """
            class Test {
                var foo: Int? = null
                    get
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var with a custom setter passes`() {
        val code = """
            class Test {
                var foo: Int
                    get() = 1
                    set(value) {
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var overriding a var passes`() {
        val code = """
            open class Base {
                open var foo: Int = 0
            }

            class Child : Base() {
                override var foo: Int = 0
                    get() = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a val with a custom getter passes`() {
        val code = """
            class Test {
                val foo: Int
                    get() = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var whose getter reads another property with a backing field is reported`() {
        val code = """
            class Test {
                var foo: Int = 0
                    get() = bar

                var bar: Int = 0
                    get() = field
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }
}
