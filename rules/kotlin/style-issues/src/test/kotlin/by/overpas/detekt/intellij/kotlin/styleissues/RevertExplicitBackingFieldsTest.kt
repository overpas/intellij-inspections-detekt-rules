package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RevertExplicitBackingFieldsTest {

    private val environment = createEnvironment()

    private val sut = RevertExplicitBackingFields(Config.empty)

    @Test
    fun `an explicit backing field of a top-level property is reported`() {
        val code = """
            val items: Set<Int>
                field = mutableSetOf<Int>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit backing field with a type is reported`() {
        val code = """
            interface I

            class C {
                val x: List<I>
                    field: MutableList<I> = mutableListOf()

                fun update(newX: I) {
                    x.add(newX)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit backing field in a companion object is reported`() {
        val code = """
            class X {
                companion object {
                    val items: Set<Int>
                        field = mutableSetOf<Int>()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit backing field initialized in an init block is reported`() {
        val code = """
            class A {
                val town: List<String>
                    field: MutableList<String>

                init {
                    town = mutableListOf()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit backing field of a final override is reported`() {
        val code = """
            interface Base {
                val names: List<String>
            }

            abstract class AbstractBase : Base {
                final override val names: List<String>
                    field = mutableListOf<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with a private backing property passes`() {
        val code = """
            class C {
                private val _items = mutableListOf<Int>()

                val items: List<Int> get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property with an initializer passes`() {
        val code = """
            val items: Set<Int> = mutableSetOf()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
