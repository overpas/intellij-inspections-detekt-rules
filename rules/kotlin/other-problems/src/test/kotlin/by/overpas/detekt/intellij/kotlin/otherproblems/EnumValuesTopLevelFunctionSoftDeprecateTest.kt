package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumValuesTopLevelFunctionSoftDeprecateTest {

    private val environment = createEnvironment()

    private val sut = EnumValuesTopLevelFunctionSoftDeprecate(Config.empty)

    @Test
    fun `an enumValues call is reported`() {
        val code = """
            enum class EnumClass

            fun foo() {
                enumValues<EnumClass>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enumValues call with an index access is reported`() {
        val code = """
            enum class MyEnum {
                A, B, C
            }

            fun listAll() {
                for (i in 0..<MyEnum.entries.size) {
                    println(enumValues<MyEnum>()[i])
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enumValues call with a chained call is reported`() {
        val code = """
            enum class MyEnum {
                A, B, C
            }

            fun findA() {
                val a: MyEnum? = enumValues<MyEnum>().firstOrNull { it == MyEnum.A }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enumValues call inside the enum is reported`() {
        val code = """
            enum class EnumClass {
                ;

                init {
                    for (e in enumValues<EnumClass>()) {}
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enumValues call passed as an array is reported`() {
        val code = """
            enum class MyEnum {
                A, B, C
            }

            fun test(arr: Array<out Any>) {
                println(arr.size)
            }

            fun main() {
                test(enumValues<MyEnum>())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread enumValues call is reported`() {
        val code = """
            enum class MyEnum

            val a: List<MyEnum> = listOf(*enumValues<MyEnum>())
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enumValues call with an inferred type argument is reported`() {
        val code = """
            enum class MyEnum

            val a: Array<MyEnum> = enumValues()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enumValues call with a reified type parameter passes`() {
        val code = """
            inline fun <reified T : Enum<T>> all(): Array<T> = enumValues<T>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom enumValues function passes`() {
        val code = """
            enum class MyEnum

            fun <T> enumValues(): List<T> = emptyList()

            val a = enumValues<MyEnum>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an enumEntries call passes`() {
        val code = """
            import kotlin.enums.enumEntries

            enum class MyEnum

            val a = enumEntries<MyEnum>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
