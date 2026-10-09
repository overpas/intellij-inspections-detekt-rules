package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class RecursivePropertyAccessorTest {

    private val environment = createEnvironment(listOf(Path("src/test/resources/RecursivePropertyAccessor")))

    private val sut = RecursivePropertyAccessor(Config.empty)

    @Test
    fun `recursive accesses in a setter are reported`() {
        val code = """
            class A {
                var x = 0
                    set(value) {
                        x++
                        x += 1
                        x = value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `recursive reads in a getter are reported`() {
        val code = $$"""
            class A {
                var y = 0
                    get() {
                        println("$y")
                        y++
                        y += 1
                        return y
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `a recursive expression getter is reported`() {
        val code = """
            class Foo {
                val p: Any
                    get() = p
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a recursive getter of a property with an initializer is reported`() {
        val code = """
            class Foo {
                var p: Any = 0
                    get() = p
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a recursive access through this in a property named field is reported`() {
        val code = """
            class A {
                var field = 0
                    get() {
                        this.field
                        return if (field != 0) field else -1
                    }
                    set(value) {
                        this.field = value
                        if (value >= 0) field = value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a recursive access through a companion qualifier is reported`() {
        val code = """
            class A {
                companion object {
                    var g = 0
                        set(value) {
                            A.g = 99
                        }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a recursive access through an object qualifier is reported`() {
        val code = """
            object Obj {
                val s: String
                    get() = Obj.s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a recursive member extension property is reported`() {
        val code = """
            class Bar

            class Foo {
                val p: Any
                    get() = 42

                val Bar.p: Any
                    get() = this.p
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a recursive top level extension property is reported`() {
        val code = """
            val p: Any = "p"
            val String.p: Any
                get() = p
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a recursive top level getter is reported`() {
        val code = """
            var p: Int = 0
                get() {
                    val base = p
                    return base + 1
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `recursive synthetic property accessors are reported`() {
        val code = """
            import a.JavaInterface

            class B {
                var something: String = "123"

                class Nested : JavaInterface {
                    override fun setSomething(value: String) {
                        val x = something
                        something = value
                    }

                    override fun getSomething(): String {
                        something = "456"
                        return something
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a callable reference to the property passes`() {
        val code = """
            interface Logger

            fun logger(f: () -> Logger): Logger = object : Logger {}

            val logger: Logger get() = logger(::logger)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a read in a setter passes`() {
        val code = """
            class A {
                var w: Int = 0
                    set(value) {
                        field = w + value
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the property of another instance passes`() {
        val code = """
            class Foo {
                val otherInstance: Foo
                    get() = null!!

                val p: Any
                    get() = otherInstance.p

                val q: Any
                    get() = with(otherInstance) { q }

                val r: Any
                    get() {
                        val v = this
                        return v.r
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the property of a smart cast instance passes`() {
        val code = """
            class Node(val next: Node?) {
                val last: Node
                    get() = if (next != null) next.last else this
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a super property passes`() {
        val code = """
            open class Base {
                open val x: Int = 1
            }

            class Derived : Base() {
                override val x: Int
                    get() {
                        return super.x
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension overload with a different receiver passes`() {
        val code = """
            class Bar

            class Foo {
                val p: Any
                    get() = 42

                val Bar.p: Any
                    get() = this@Foo.p
            }

            val Any.q: Any
                get() = "q".q
            val String.q: Any
                get() = this
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension property of another receiver in a setter passes`() {
        val code = """
            class Storage(var v: Int)
            private val storage = Storage(0)

            var Storage.foo: Int
                get() = v
                set(value) { v = value }

            var foo: Int
                get() = with(storage) { foo }
                set(value: Int) { storage.foo = value }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
