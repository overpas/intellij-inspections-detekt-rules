package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinArrayToStringTest {

    private val environment = createEnvironment()

    private val sut = KotlinArrayToString(Config.empty)

    @Test
    fun `toString on an object array is reported`() {
        val code = """
            fun main() {
                val array = arrayOf(1, 2, 3)
                val text = array.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `toString on a primitive array is reported`() {
        val code = """
            fun main() {
                val array = intArrayOf(1, 2, 3)
                val text = array.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `toString on a generic vararg parameter is reported`() {
        val code = """
            fun <T> stringify(vararg items: T): String = items.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe toString call on a nullable array is reported`() {
        val code = """
            fun main() {
                val array: IntArray? = intArrayOf(1, 2, 3)
                val text = array?.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a string concatenated with an array is reported`() {
        val code = """
            fun main() {
                val array = arrayOf(arrayOf(1, 2), arrayOf(3, 4))
                val text = "Nested: " + array
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an array in a string template is reported`() {
        val code = $$"""
            fun main() {
                val array = arrayOf(1, 2, 3)
                val simple = "Array: $array"
                val block = "Array: ${array}"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `print and println of an array are reported`() {
        val code = """
            fun main() {
                print(arrayOf("a", "b", "c"))
                println(doubleArrayOf(1.0, 2.0))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `appending an array to a string builder is reported`() {
        val code = """
            fun main() {
                StringBuilder().append(arrayOf(1, 2, 3))
                StringBuffer().append(intArrayOf(1, 2, 3))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `printing an array to a print stream and a print writer is reported`() {
        val code = """
            import java.io.PrintWriter

            fun write(writer: PrintWriter) {
                System.out.println(intArrayOf(1))
                writer.print(arrayOf(1))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `printing and appending a char array passes`() {
        val code = """
            import java.io.PrintWriter

            fun write(writer: PrintWriter) {
                val chars = charArrayOf('h', 'i')
                print(chars)
                println(chars)
                System.out.println(chars)
                writer.print(chars)
                StringBuilder().append(chars)
                StringBuffer().append(chars)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `contentToString on an array passes`() {
        val code = $$"""
            fun main() {
                val array = arrayOf(1, 2, 3)
                val text = "Array: ${array.contentToString()}" + array.contentDeepToString()
                println(array.contentToString())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `toString on a string passes`() {
        val code = """
            fun main() {
                val text = "test".toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an array concatenated with a string passes`() {
        val code = """
            fun main() {
                val array = arrayOf("a")
                val result = array + "b"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a list in a string template passes`() {
        val code = $$"""
            fun main() {
                val list = listOf(1, 2, 3)
                println("List: $list")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
