package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CanBePrimaryConstructorPropertyTest {

    private val environment = createEnvironment()

    private val sut = CanBePrimaryConstructorProperty(Config.empty)

    @Test
    fun `a property assigned from the parameter with the same name and type is reported`() {
        val code = """
            class Correct(name: String) {
                val name: String = name
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with an inferred type is reported`() {
        val code = """
            class Complex(x: Int, y: Double, z: String) {
                val x = x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a protected open var property is reported`() {
        val code = """
            open class Container(index: Int) {
                protected open var index = index
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property assigned from a vararg parameter is reported`() {
        val code = """
            class A(vararg strings: String) {
                val strings = strings
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with a supertype of the parameter type passes`() {
        val code = """
            class SomeClass(stringValue: String) {
                var stringValue: Any = stringValue
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property with a nullable parameter type passes`() {
        val code = """
            class SomeClass(stringValue: String) {
                var stringValue: String? = stringValue

                fun someFun() {
                    stringValue = null
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property assigned from a parameter with another name passes`() {
        val code = """
            class SomeClass(value: String) {
                val name = value
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property with a getter passes`() {
        val code = """
            class SomeClass(name: String) {
                val name = name
                    get() = field.trim()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property in a class with a secondary constructor passes`() {
        val code = """
            class SomeClass(name: String) {
                val name = name

                constructor() : this("")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property of an object passes`() {
        val code = """
            fun create(name: String) = object {
                val name = name
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parameter whose type refers to the class itself passes`() {
        val code = """
            class Node(parent: Node?) {
                val parent = parent
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
