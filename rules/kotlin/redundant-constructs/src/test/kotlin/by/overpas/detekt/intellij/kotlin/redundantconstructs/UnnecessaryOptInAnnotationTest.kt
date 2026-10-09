package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnnecessaryOptInAnnotationTest {

    private val environment = createEnvironment()

    private val sut = UnnecessaryOptInAnnotation(Config.empty)

    @Test
    fun `an opt-in without any experimental usage is reported`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @OptIn(Marker::class)
            class Foo
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an opt-in on a function that uses no experimental API is reported`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @Marker
            fun experimentalApi() {}

            @OptIn(Marker::class)
            fun useNothing() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an opt-in with one of two markers unused is reported`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @RequiresOptIn
            annotation class AnotherMarker

            @AnotherMarker
            fun bar() {}

            @OptIn(Marker::class, AnotherMarker::class)
            class Foo {
                fun foo() {
                    bar()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an opt-in with an import alias is reported`() {
        val code = """
            import kotlin.OptIn as Consent

            @RequiresOptIn
            annotation class Marker

            @Marker
            fun foo() {}

            @Consent(Marker::class)
            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an opt-in for a property read of a marked setter is reported`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            class Foo {
                var bar: Int = 0
                    @Marker
                    set(value) {
                        field = value
                    }
            }

            @OptIn(Marker::class)
            fun baz(foo: Foo): Int {
                return foo.bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an opt-in for a val delegate with a marked setter is reported`() {
        val code = """
            import kotlin.reflect.KProperty

            @RequiresOptIn
            annotation class Marker

            class Delegate {
                operator fun getValue(instance: Any?, property: KProperty<*>): String = ""

                @Marker
                operator fun setValue(instance: Any?, property: KProperty<*>, value: String) {}
            }

            @OptIn(Marker::class)
            val foo by Delegate()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an opt-in used by a function call passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @Marker
            fun foo(x: Int): Int = x + 1

            @OptIn(Marker::class)
            fun bar(n: Int) = foo(n)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a parameter type passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @Marker
            class A

            @OptIn(Marker::class)
            fun bar(a: A) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a constructor call passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @Marker
            class Foo

            @OptIn(Marker::class)
            fun foo() {
                val x = Foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a marked annotation passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @Marker
            annotation class FooAnnotation

            @OptIn(Marker::class)
            object Bar {
                @FooAnnotation
                fun bar() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by an override passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            open class Base {
                @Marker
                open fun foo() {}
            }

            class Derived : Base() {
                @OptIn(Marker::class)
                override fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a property setter write passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            class Foo {
                var bar: Int = 0
                    @Marker
                    set(value) {
                        field = value
                    }
            }

            @OptIn(Marker::class)
            fun baz(foo: Foo) {
                foo.bar += 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a delegate getter passes`() {
        val code = """
            import kotlin.reflect.KProperty

            @RequiresOptIn
            annotation class Marker

            class Delegate {
                @Marker
                operator fun getValue(instance: Any?, property: KProperty<*>): String = ""
            }

            @OptIn(Marker::class)
            val foo by Delegate()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a marked type alias passes`() {
        val code = """
            @RequiresOptIn
            @Target(AnnotationTarget.TYPEALIAS)
            annotation class Marker

            class A {
                fun foo() {}
            }

            @Marker
            typealias B = A

            @OptIn(Marker::class)
            fun bar() {
                val x = B()
                x.foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a companion member of a marked class passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @Marker
            class Base64 {
                companion object {
                    fun encode(source: ByteArray): String = ""
                }
            }

            @OptIn(Marker::class)
            fun String.toBase64() = Base64.encode(toByteArray())
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a subclass opt-in required supertype passes`() {
        val code = """
            @RequiresOptIn
            annotation class Marker

            @SubclassOptInRequired(Marker::class)
            interface CoreLibraryApi

            @OptIn(Marker::class)
            class SomeImplementation : CoreLibraryApi
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in used by a vararg of an experimental array type passes`() {
        val code = """
            @OptIn(ExperimentalUnsignedTypes::class)
            fun foo(vararg values: ULong) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an opt-in without arguments passes`() {
        val code = """
            @OptIn
            fun someFun() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
