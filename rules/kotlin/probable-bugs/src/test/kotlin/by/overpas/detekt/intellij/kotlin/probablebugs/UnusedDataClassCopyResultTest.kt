package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedDataClassCopyResultTest {

    private val environment = createEnvironment()

    private val sut = UnusedDataClassCopyResult(Config.empty)

    @Test
    fun `an unused qualified copy call is reported`() {
        val code = """
            data class Foo(val prop: String)

            fun bar(foo: Foo) {}

            fun main() {
                val o = Foo("")
                o.copy(prop = "New")
                bar(o)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused copy call on an implicit receiver is reported`() {
        val code = """
            data class Foo(val prop: String)

            fun bar(foo: Foo) {}

            fun main() {
                val o = Foo("")
                o.run {
                    copy(prop = "New")
                    bar(o)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused safe copy call is reported`() {
        val code = """
            data class Foo(val prop: String)

            fun main(o: Foo?) {
                o?.copy(prop = "New")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a used copy call passes`() {
        val code = """
            data class Foo(val prop: String)

            fun bar(foo: Foo) {}

            fun main() {
                val o = Foo("")
                val o2 = o.copy(prop = "New")
                bar(o2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a used copy call on an implicit receiver passes`() {
        val code = """
            data class Foo(val prop: String)

            fun bar(foo: Foo) {}

            fun main() {
                val o = Foo("")
                o.run {
                    val o2 = copy(prop = "New")
                    bar(o2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a returned copy call passes`() {
        val code = """
            data class Foo(val prop: String)

            fun rename(o: Foo) = o.copy(prop = "New")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused extension copy call passes`() {
        val code = """
            data class Foo(val prop: String)

            fun Foo.copy(num: Int) = Unit

            fun main() {
                val o = Foo("")
                o.copy(num = 42)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused copy call of a regular class passes`() {
        val code = """
            class Foo(val prop: String) {
                fun copy(prop: String) = Foo(prop)
            }

            fun main() {
                val o = Foo("")
                o.copy(prop = "New")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
