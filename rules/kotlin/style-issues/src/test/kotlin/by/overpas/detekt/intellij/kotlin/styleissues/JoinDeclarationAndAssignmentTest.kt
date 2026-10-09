package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class JoinDeclarationAndAssignmentTest {

    private val environment = createEnvironment()

    private val sut = JoinDeclarationAndAssignment(Config.empty)

    @Test
    fun `a local value assigned right after its declaration is reported`() {
        val code = """
            fun foo(): String {
                val s: String
                s = "Hello"
                return s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a local value assigned after another statement is reported`() {
        val code = """
            fun test(repo: Repository, commitMessage: String): String {
                val hash: String
                repo.git("add --verbose .")
                hash = repo.git("commit -m " + commitMessage)
                return hash
            }

            class Repository {
                fun git(s: String) = s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member property assigned first in an init block is reported`() {
        val code = """
            class A {
                var a: Int
                var b: Int

                init {
                    a = 1
                    b = 2
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member property assigned in the only secondary constructor is reported`() {
        val code = """
            class A {
                constructor() {
                    a = 1
                    foo()
                }

                val a: Int

                fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member property assigned from a constructor parameter is reported`() {
        val code = """
            class A(prop: Any) {
                val prop: Any

                init {
                    this.prop = prop
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member property assigned with a subtype is reported`() {
        val code = """
            class Temp {
                private val listField: MutableList<Int>

                init {
                    listField = ArrayList()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a local lateinit variable assigned twice is reported`() {
        val code = """
            fun foo(a: String, b: String): String {
                lateinit var c: String
                c = a
                c = b
                return c
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable assigned inside an if passes`() {
        val code = """
            fun foo(flag: Boolean): Double {
                var x: Double
                if (flag) {
                    x = 3.14
                }
                x = 2.71
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable first assigned in a local function passes`() {
        val code = """
            fun bar() {
                var x: String
                fun foo() {
                    x = "456"
                    x.hashCode()
                }
                foo()
                x = "123"
                x.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an initializer that uses a later declaration passes`() {
        val code = """
            fun foo(): Double {
                val x: Double
                val flag = false
                x = if (flag) 3.14 else 2.71
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member property assigned in several constructors passes`() {
        val code = """
            class A {
                constructor() {
                    a = 1
                }

                constructor(aa: Int) {
                    a = aa
                }

                val a: Int
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member property assigned after other statements of an init block passes`() {
        val code = """
            class V(private val values: MutableList<Int>) {
                val a: Int

                init {
                    values.add(1)
                    a = values.size
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member property assigned from a secondary constructor parameter passes`() {
        val code = """
            class Id {
                val id: Int

                constructor(id: Int) {
                    this.id = id
                }

                constructor() : this(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local lateinit variable used before the assignment passes`() {
        val code = """
            fun test() {
                lateinit var info: String
                addActionListener {
                    println(info)
                }
                info = ""
            }

            fun addActionListener(callback: () -> Unit) {
                callback()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local lateinit variable used in its own initializer passes`() {
        val code = """
            fun foo(o: Any) {
                println(o)
            }

            fun bar() {
                lateinit var lambda: () -> Unit
                lambda = {
                    foo(lambda)
                }
                lambda()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member property that uses a later member passes`() {
        val code = """
            class A {
                private val a: Int
                private val b = 1

                init {
                    a = b + b
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
