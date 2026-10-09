package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SetterBackingFieldAssignmentTest {

    private val environment = createEnvironment()

    private val sut = SetterBackingFieldAssignment(Config.empty)

    @Test
    fun `an empty setter of a property with an initializer is reported`() {
        val code = """
            class Test {
                var foo: Int = 1
                    set(value) {
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an empty setter with a typed parameter is reported`() {
        val code = """
            class Test {
                var foo: Int = 1
                    set(value: Int) {
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter that only reads the field is reported`() {
        val code = """
            class Test {
                var foo: Int = 1
                    set(value) {
                        bar(field)
                    }

                fun bar(i: Int) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter that assigns another property is reported`() {
        val code = """
            class Test {
                private var str1: String? = null
                private var str2: String? = null
                    set(value) { str1 = value }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter of a top level property with a backticked parameter is reported`() {
        val code = """
            var value: Int = 0
                set(`T _ T`) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter that assigns the field passes`() {
        val code = """
            class Test {
                var foo: Int = 1
                    set(value) {
                        bar()
                        field = value
                    }

                fun bar() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `setters with augmented assignments of the field pass`() {
        val code = """
            class Test {
                var a: Int = 10
                    set(value) {
                        field += value
                    }
                var b: Int = 10
                    set(value) {
                        field -= value
                    }
                var c: Int = 10
                    set(value) {
                        field *= value
                    }
                var d: Int = 10
                    set(value) {
                        field /= value
                    }
                var e: Int = 10
                    set(value) {
                        field %= value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `setters with increments and decrements of the field pass`() {
        val code = """
            class Test {
                var a: Int = 10
                    set(value) {
                        field++
                    }
                var b: Int = 10
                    set(value) {
                        ++field
                    }
                var c: Int = 10
                    set(value) {
                        field--
                    }
                var d: Int = 10
                    set(value) {
                        --field
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter that passes the value to a function passes`() {
        val code = """
            class Test {
                var foo: Int = 10
                    set(value) {
                        bar(value)
                    }

                fun bar(value: Int) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter that passes the value as a named argument passes`() {
        val code = """
            class Test {
                var foo: Int = 10
                    set(value: Int) {
                        bar(value = value)
                    }

                fun bar(value: Int) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter of a property without a backing field passes`() {
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
    fun `a setter that throws passes`() {
        val code = """
            class Test {
                var test = "OK"
                    set(value) {
                        throw UnsupportedOperationException()
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
