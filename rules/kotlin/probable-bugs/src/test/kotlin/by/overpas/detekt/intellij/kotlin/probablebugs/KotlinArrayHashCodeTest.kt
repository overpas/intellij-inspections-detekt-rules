package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinArrayHashCodeTest {

    private val environment = createEnvironment()

    private val sut = KotlinArrayHashCode(Config.empty)

    @Test
    fun `hashCode on an object array is reported`() {
        val code = """
            fun main() {
                val array = arrayOf<Any>()
                val hash = array.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `hashCode on a primitive array is reported`() {
        val code = """
            fun main() {
                val array = intArrayOf(1, 2, 3)
                val hash = array.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `hashCode on a generic array is reported`() {
        val code = """
            fun <T> hashArray(array: Array<T>): Int {
                return array.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `hashCode on a nested array is reported`() {
        val code = """
            fun main() {
                val array = arrayOf(arrayOf(1, 2), arrayOf(3, 4))
                val hash = array.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe hashCode call on a nullable array is reported`() {
        val code = """
            fun main() {
                val array: IntArray? = intArrayOf(1, 2, 3)
                val hash = array?.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `hashCode on a vararg parameter is reported`() {
        val code = """
            fun hashItems(vararg items: Int): Int = items.hashCode()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `hashCode on a string passes`() {
        val code = """
            fun main() {
                val notArray = "test"
                val hash = notArray.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `contentHashCode on an array passes`() {
        val code = """
            fun main() {
                val array = intArrayOf(1, 2, 3)
                val hash = array.contentHashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `hashCode on a list passes`() {
        val code = """
            fun main() {
                val list = listOf(1, 2, 3)
                val hash = list.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension hashCode with an argument on an array passes`() {
        val code = """
            fun IntArray.hashCode(seed: Int): Int = seed + size

            fun main() {
                val hash = intArrayOf(1).hashCode(31)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
