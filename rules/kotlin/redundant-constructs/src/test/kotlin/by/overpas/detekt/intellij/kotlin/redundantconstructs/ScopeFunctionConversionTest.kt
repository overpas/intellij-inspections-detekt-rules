package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ScopeFunctionConversionTest {

    private val environment = createEnvironment()

    private val sut = ScopeFunctionConversion(Config.empty)

    @Test
    fun `apply is reported`() {
        val code = """
            val x = "".apply {
                this.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `also is reported`() {
        val code = """
            val x = "".also {
                it.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `let is reported`() {
        val code = """
            val x = "".let {
                it.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `run with a receiver is reported`() {
        val code = """
            val x = "".run {
                length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `with is reported`() {
        val code = $$"""
            class User(val name: String, val age: Int)

            fun test(user: User) {
                val a = with(user) {
                    "User $name, age $age"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `let with a named parameter is reported`() {
        val code = """
            fun main() {
                val text: String = "Kotlin"
                val length = text.let { t ->
                    val trimmed = t.trim()
                    trimmed.length
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `let with a destructuring parameter passes`() {
        val code = $$"""
            data class UserData(val name: String, val age: Int)

            fun test(data: UserData) {
                val result = data.let { (name, age) ->
                    "User $name, age $age"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `let with a named parameter used in a string template passes`() {
        val code = $$"""
            class User(val name: String, val age: Int)

            fun test(user: User) {
                val result = user.let { userData ->
                    "User ${userData.name}, age ${userData.age}"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `let with a named parameter on a nullable receiver passes`() {
        val code = """
            fun test() {
                val text: String? = "  Kotlin  "
                val length = text?.let { t ->
                    val trimmed = t.trim()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `run without a receiver passes`() {
        val code = """
            fun hello() {
                run { }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `apply whose counterpart is shadowed by a member passes`() {
        val code = """
            class Foo {
                fun also(block: () -> Unit) {}

                fun test() {
                    apply { toString() }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
