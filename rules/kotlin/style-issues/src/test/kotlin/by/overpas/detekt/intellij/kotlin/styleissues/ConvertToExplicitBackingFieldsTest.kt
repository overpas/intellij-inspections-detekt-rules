package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertToExplicitBackingFieldsTest {

    private val environment = createEnvironment()

    private val sut = ConvertToExplicitBackingFields(Config.empty)

    @Test
    fun `a top-level property that returns a private mutable set is reported`() {
        val code = """
            private val _items = mutableSetOf<Int>()
            val items: Set<Int>
                get() = _items
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a class property that returns a private subtype is reported`() {
        val code = """
            interface A
            class B : A

            class C {
                private val _items = B()
                val items: A get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter that returns the backing property through this is reported`() {
        val code = """
            class UserViewModel {
                private val _names: MutableSet<String>

                val x: Set<String>
                    get() = this._names

                init {
                    this._names = mutableSetOf("John")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter with a block body that returns the backing property is reported`() {
        val code = """
            class Foo {
                private val _x = mutableListOf<Int>()
                val x: List<Int>
                    get() {
                        return _x
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final override in an abstract class is reported`() {
        val code = """
            interface Base {
                val names: List<String>
            }

            abstract class AbstractBase : Base {
                private val _names = mutableListOf<String>()
                final override val names: List<String> get() = _names
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of a sealed class that is not open is reported`() {
        val code = """
            sealed class Test {
                private val _items = mutableSetOf<Int>()

                val items: Set<Int>
                    get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with an out-projected generic type is reported`() {
        val code = """
            class MyClass<out T>

            private val _y = MyClass<String>()
            val y: MyClass<Any>
                get() = _y
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backing property of the same type passes`() {
        val code = """
            class MyClass

            private val _x = MyClass()
            val x: MyClass
                get() = _x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backing property that is a var passes`() {
        val code = """
            class Foo {
                private var _x = mutableListOf<Int>()
                val x: List<Int> get() = _x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backing property with a getter passes`() {
        val code = """
            class A {
                private val _storage: String
                    get() = "dynamic"

                val data: CharSequence
                    get() = _storage
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegated backing property passes`() {
        val code = """
            class Foo {
                private val _x by lazy { mutableListOf(1, 2, 3) }
                val x: List<Int> get() = _x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backing property that is not private passes`() {
        val code = """
            val _x = mutableListOf<String>()
            val x: List<String> get() = _x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with more logic passes`() {
        val code = """
            private val _x = mutableListOf<String>()
            val x: List<String> get() {
                return _x + listOf("a", "b")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension property passes`() {
        val code = """
            class Holder

            private val _items = mutableSetOf<Int>()

            val Holder.items: Set<Int>
                get() = _items
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open property passes`() {
        val code = """
            open class Test {
                private val _items = mutableSetOf<Int>()

                open val items: Set<Int>
                    get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override in an abstract class passes`() {
        val code = """
            interface Base {
                val names: List<String>
            }

            abstract class AbstractBase : Base {
                private val _names = mutableListOf<String>()
                override val names: List<String> get() = _names
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var property passes`() {
        val code = """
            private val _x = mutableListOf<String>()
            var x: List<String>
                get() = _x
                set(value) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
