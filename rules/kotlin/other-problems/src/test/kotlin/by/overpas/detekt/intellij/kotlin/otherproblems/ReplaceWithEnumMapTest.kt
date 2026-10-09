package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceWithEnumMapTest {

    private val environment = createEnvironment()

    private val sut = ReplaceWithEnumMap(Config.empty)

    @Test
    fun `a HashMap constructor with an expected enum key is reported`() {
        val code = """
            enum class E {
                A, B
            }

            fun main() {
                val test: Map<E, String> = HashMap()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an imported java HashMap constructor is reported`() {
        val code = """
            import java.util.HashMap

            enum class E {
                A, B
            }

            fun getMap(): Map<E, String> = HashMap()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a fully qualified HashMap constructor is reported`() {
        val code = """
            enum class E {
                A, B
            }

            fun getMap(): Map<E, String> = java.util.HashMap()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a hashMapOf call with an inferred enum key is reported`() {
        val code = """
            enum class E {
                A, B
            }

            fun getMap(): Map<E, String> = hashMapOf()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a hashMapOf call through an import alias is reported`() {
        val code = """
            import kotlin.collections.hashMapOf as foo

            enum class E {
                A, B
            }

            fun getMap(): Map<E, String> = foo()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a HashMap constructor assigned to a mutable map property is reported`() {
        val code = """
            enum class E {
                A, B
            }

            class Holder {
                val map: MutableMap<E, Int> = HashMap()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a hashMapOf call without an expected type passes`() {
        val code = """
            enum class E {
                A, B
            }

            fun main() {
                val map = hashMapOf<E, String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a hashMapOf call with arguments passes`() {
        val code = """
            fun main() {
                val map: Map<Int, Int> = hashMapOf(5 to 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a HashMap constructor with a capacity passes`() {
        val code = """
            enum class E {
                A, B
            }

            fun getMap(): Map<E, String> = HashMap(4)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a HashMap constructor with a non-enum key passes`() {
        val code = """
            fun getMap(): Map<String, Int> = HashMap()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a LinkedHashMap constructor with an enum key passes`() {
        val code = """
            enum class E {
                A, B
            }

            fun getMap(): Map<E, String> = LinkedHashMap()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
