package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertReferenceToLambdaTest {

    private val environment = createEnvironment()

    private val sut = ConvertReferenceToLambda(Config.empty)

    @Test
    fun `a reference to a top-level function is reported`() {
        val code = """
            fun foo(y: Int) = y

            val x = ::foo
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to a member function passed to apply is reported`() {
        val code = """
            class My {
                fun foo() {}
            }

            val x = My().apply(My::foo)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a bound reference is reported`() {
        val code = """
            val x = 1

            val y = x::hashCode
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor reference passed to map is reported`() {
        val code = """
            class Person(val name: String)

            val x = listOf("Jack", "Tom").map(::Person)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property reference passed to map is reported`() {
        val code = """
            val x = listOf("123", "4567").map(String::length)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an object member reference is reported`() {
        val code = """
            val list = listOf(1, 2, 3).map(Utils::foo)

            object Utils {
                fun foo(x: Int) = x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to an extension function with a nullable receiver is reported`() {
        val code = """
            fun Int?.foo() = this?.hashCode() ?: 0

            val x = Int?::foo
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to an extension property is reported`() {
        val code = """
            val Any.name: String get() = toString()

            val converted = Any::name
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference with a function type as expected type is reported`() {
        val code = """
            class Foo {
                class Bar {
                    fun foo() {}
                }
            }

            fun use() {
                val f: (Foo.Bar) -> Unit = Foo.Bar::foo
                f(Foo.Bar())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `references in both branches of a when are reported`() {
        val code = """
            class Test {
                fun bar() = 1

                fun test(x: Int): () -> Int = when (x) {
                    1 -> this::bar
                    else -> this::bar
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a reference passed as a KFunction passes`() {
        val code = """
            import kotlin.reflect.KFunction

            fun <P : Any> p(p: KFunction<P>) {}

            class B {
                fun getS(): String = ""

                init {
                    p(this::getS)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference passed as a KProperty passes`() {
        val code = """
            import kotlin.reflect.KProperty

            fun <P : Any> p(p: KProperty<P>) {}

            class B {
                val s: String = ""

                init {
                    p(this::s)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference assigned to a KProperty0 passes`() {
        val code = """
            import kotlin.reflect.KProperty0

            class B {
                val s: String = ""

                val reference: KProperty0<String> = this::s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a class literal passes`() {
        val code = """
            val x = String::class
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda passes`() {
        val code = """
            fun foo(y: Int) = y

            val x = listOf(1, 2).map { foo(it) }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
