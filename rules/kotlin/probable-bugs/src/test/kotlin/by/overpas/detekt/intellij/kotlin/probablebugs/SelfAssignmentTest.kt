package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SelfAssignmentTest {

    private val environment = createEnvironment()

    private val sut = SelfAssignment(Config.empty)

    @Test
    fun `a local var assigned to itself is reported`() {
        val code = """
            fun test() {
                var bar = 1
                bar = bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property assigned to itself is reported`() {
        val code = """
            class Test {
                var foo = 1

                fun test() {
                    foo = foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property assigned to itself through this is reported`() {
        val code = """
            class Test {
                var foo = 1

                fun test() {
                    foo = this.foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property assigned to itself through a labeled this is reported`() {
        val code = """
            class Test {
                var foo = 1

                fun test() {
                    this.foo = this@Test.foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of a parameter assigned to itself is reported`() {
        val code = """
            class Test {
                var foo = 1
            }

            fun f(a: Test) {
                a.foo = a.foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegated property assigned to itself is reported`() {
        val code = """
            import kotlin.properties.ReadWriteProperty
            import kotlin.reflect.KProperty

            class Test {
                var foo: Int by Delegate()

                fun test() {
                    foo = foo
                }
            }

            class Delegate : ReadWriteProperty<Test, Int> {
                override fun getValue(thisRef: Test, property: KProperty<*>): Int = 1
                override fun setValue(thisRef: Test, property: KProperty<*>, value: Int) {
                    println()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property assigned to itself inside a scope function is reported`() {
        val code = """
            class Test {
                var foo = 1

                fun test() {
                    with(Test()) {
                        this.foo = foo
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of the outer instance assigned to itself inside apply is reported`() {
        val code = """
            class ServerUser {
                var id = ""
                var city = ""

                fun toClientUser() = ClientUser().apply {
                    id = this@ServerUser.id
                    city = this@ServerUser.city
                }
            }

            class ClientUser {
                var id = ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of a smart cast receiver assigned to itself is reported`() {
        val code = """
            class Test {
                var foo = 1
            }

            fun Any.test() {
                if (this is Test) {
                    this.foo = foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of a smart cast parameter assigned to itself is reported`() {
        val code = """
            class Foo {
                var foo: Int = 1
            }

            fun test(a: Any) {
                if (a is Foo) {
                    a.foo = a.foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an augmented assignment passes`() {
        val code = """
            fun test() {
                var bar = 1
                bar += bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of another instance passes`() {
        val code = """
            class Test {
                var foo = 1
            }

            fun f(a: Test, b: Test) {
                a.foo = b.foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of an implicit receiver assigned from the outer instance passes`() {
        val code = """
            class Test {
                var foo = 1

                fun test() {
                    with(Test()) {
                        this@Test.foo = foo
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an indexed assignment passes`() {
        val code = """
            fun test() {
                val list = mutableListOf(1, 2, 3)
                list[1] = list[1]
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open property passes`() {
        val code = """
            open class Test {
                open var foo = 1

                fun test() {
                    foo = this.foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property with a custom setter passes`() {
        val code = """
            class Test {
                var foo = 1
                    set(value) {
                        println(value)
                    }

                fun test() {
                    foo = foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property with a custom getter passes`() {
        val code = """
            class Test {
                var foo = 1
                    get() {
                        println()
                        return 2
                    }

                fun test() {
                    foo = foo
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of a cast receiver passes`() {
        val code = """
            abstract class A {
                abstract fun merge(a: A)
            }

            class B : A() {
                var v: Int = 0

                override fun merge(a: A) {
                    v = (a as B).v
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of a constructor call passes`() {
        val code = """
            class Foo {
                var bar: String = ""
            }

            fun action() {
                Foo().bar = Foo().bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of the outer instance inside apply on another instance passes`() {
        val code = """
            class A {
                var num: Int = 10

                fun test() {
                    A().apply {
                        num = this@A.num
                        println(num)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of a smart cast labeled this inside a scope function is reported`() {
        val code = """
            class Foo {
                var foo: Int = 1
            }

            fun Any.test() {
                if (this is Foo) {
                    with(Foo()) {
                        this@test.foo = this@test.foo
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of a smart cast labeled this assigned from another implicit receiver passes`() {
        val code = """
            class Foo {
                var foo: Int = 1
            }

            fun Any.test() {
                if (this is Foo) {
                    with(Foo()) {
                        this@test.foo = foo
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
