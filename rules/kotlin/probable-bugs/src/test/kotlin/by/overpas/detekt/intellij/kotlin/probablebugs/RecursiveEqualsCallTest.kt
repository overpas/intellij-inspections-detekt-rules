package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RecursiveEqualsCallTest {

    private val environment = createEnvironment()

    private val sut = RecursiveEqualsCall(Config.empty)

    @Test
    fun `an equality check of this and the parameter is reported`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    if (this == other) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an inequality check of this and the parameter is reported`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    if (this != other) return false
                    return true
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call on this with the parameter is reported`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    if (this.equals(other)) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit equals call with the parameter is reported`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    if (equals(other)) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe equals call on this with the parameter is reported`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    if (this?.equals(other) == true) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check of a labeled this of the same class and the parameter is reported`() {
        val code = """
            open class Outer {
                inner class Nested : Outer() {
                    override fun equals(other: Any?): Boolean {
                        if (this@Nested == other) return true
                        return false
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check of this and a local value passes`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    val s = Test()
                    if (this == s) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check of another instance and the parameter passes`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    val another = Test()
                    if (another == other) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals call on another instance with the parameter passes`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    val another = Test()
                    if (another.equals(other)) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check of an outer this and the parameter passes`() {
        val code = """
            open class Outer {
                inner class Nested : Outer() {
                    override fun equals(other: Any?): Boolean {
                        if (this@Outer == other) return true
                        return false
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a referential equality check of this and the parameter passes`() {
        val code = """
            class Test {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check of this and the parameter outside of equals passes`() {
        val code = """
            class Test {
                fun isSame(other: Any?): Boolean = this == other
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check of this and the parameter in an equals overload passes`() {
        val code = """
            class Test {
                fun equals(other: Test): Boolean = this == other
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
