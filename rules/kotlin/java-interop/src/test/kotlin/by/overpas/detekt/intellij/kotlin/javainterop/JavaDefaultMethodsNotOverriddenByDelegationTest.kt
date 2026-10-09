package by.overpas.detekt.intellij.kotlin.javainterop

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaDefaultMethodsNotOverriddenByDelegationTest {

    private val environment = createEnvironment()

    private val sut = JavaDefaultMethodsNotOverriddenByDelegation(Config.empty)

    @Test
    fun `a delegate of a final class that overrides a default method is reported`() {
        val code = """
            import java.util.function.Consumer

            class Impl : Consumer<String> {
                override fun accept(t: String) {}
                override fun andThen(after: Consumer<in String>): Consumer<String> = this
            }

            class Foo(val impl: Impl) : Consumer<String> by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegate of an object expression is reported`() {
        val code = """
            import java.util.function.Consumer

            class Impl : Consumer<String> {
                override fun accept(t: String) {}
                override fun andThen(after: Consumer<in String>): Consumer<String> = this
            }

            fun test() {
                val impl = Impl()
                object : Consumer<String> by impl {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegate of an open class is reported`() {
        val code = """
            import java.util.function.Consumer

            open class Impl : Consumer<String> {
                override fun accept(t: String) {}
            }

            class Foo(val impl: Impl) : Consumer<String> by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegate of the interface type is reported`() {
        val code = """
            import java.util.function.Consumer

            class Foo(consumer: Consumer<String>) : Consumer<String> by consumer
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegate of a sealed class with an inheritor that overrides a default method is reported`() {
        val code = """
            import java.util.function.Consumer

            sealed class Impl : Consumer<String> {
                override fun accept(t: String) {}
            }

            class A : Impl()

            class B : Impl() {
                override fun andThen(after: Consumer<in String>): Consumer<String> = this
            }

            class Foo(val impl: Impl) : Consumer<String> by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegate of a final class without overrides of default methods passes`() {
        val code = """
            import java.util.function.Consumer

            class Impl : Consumer<String> {
                override fun accept(t: String) {}
            }

            class Foo(val impl: Impl) : Consumer<String> by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegate of a sealed class without overrides of default methods passes`() {
        val code = """
            import java.util.function.Consumer

            sealed class Impl : Consumer<String> {
                override fun accept(t: String) {}
            }

            class A : Impl()

            class B : Impl()

            class Foo(val impl: Impl) : Consumer<String> by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegated interface without default methods passes`() {
        val code = """
            open class Impl : Runnable {
                override fun run() {}
            }

            fun test() {
                val impl = Impl()
                object : Runnable by impl {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a default method of an interface that is not delegated passes`() {
        val code = """
            import java.util.function.Consumer

            open class Impl : Runnable {
                override fun run() {}
            }

            fun test() {
                val impl = Impl()
                object : Consumer<String>, Runnable by impl {
                    override fun accept(t: String) {}
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a class that overrides the default methods itself passes`() {
        val code = """
            import java.util.function.Consumer

            open class Impl : Consumer<String> {
                override fun accept(t: String) {}
            }

            class Foo(val impl: Impl) : Consumer<String> by impl {
                override fun andThen(after: Consumer<in String>): Consumer<String> = impl.andThen(after)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegated Kotlin interface with a default method passes`() {
        val code = """
            interface Greeter {
                fun name(): String
                fun greet(): String = "Hello"
            }

            open class Impl : Greeter {
                override fun name() = "Impl"
            }

            class Foo(val impl: Impl) : Greeter by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
