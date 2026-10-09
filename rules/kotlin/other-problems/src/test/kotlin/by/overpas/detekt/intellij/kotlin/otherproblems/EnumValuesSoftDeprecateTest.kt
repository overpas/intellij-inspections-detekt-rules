package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumValuesSoftDeprecateTest {

    private val environment = createEnvironment()

    private val sut = EnumValuesSoftDeprecate(Config.empty)

    @Test
    fun `a qualified values call is reported`() {
        val code = """
            enum class EnumClass

            fun foo() {
                EnumClass.values()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a values call with an index access is reported`() {
        val code = """
            enum class EnumClass { VAL }

            fun foo() {
                val a = EnumClass.values()[0]
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a values call in a for loop is reported`() {
        val code = """
            enum class EnumClass

            fun foo() {
                for (el in EnumClass.values()) { }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a values call with a chained call is reported`() {
        val code = """
            enum class EnumClass

            fun foo() {
                EnumClass.values().forEach {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unqualified values call inside the enum is reported`() {
        val code = """
            enum class EnumClass {
                ONE;

                init {
                    values().forEach {}
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a values call on a stdlib enum is reported`() {
        val code = """
            fun foo() {
                AnnotationTarget.values()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a values call through a type alias is reported`() {
        val code = """
            typealias EnumClassAlias = EnumClass

            enum class EnumClass

            fun foo() {
                EnumClassAlias.values()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a values call with a fully qualified enum name is reported`() {
        val code = """
            package com.example

            enum class EnumClass

            fun foo() {
                for (e in com.example.EnumClass.values()) { }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a statically imported values call is reported`() {
        val code = """
            import EnumClass.values

            private enum class EnumClass

            fun foo() {
                values()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread values call is reported`() {
        val code = """
            enum class EnumClass

            val a: List<EnumClass> = listOf(*EnumClass.values())
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a custom values function with a parameter passes`() {
        val code = """
            enum class EnumClass {
                ;

                companion object {
                    fun values(p: Int): Array<EnumClass> = emptyArray()
                }
            }

            fun foo() {
                EnumClass.values(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a values function of a non-enum class passes`() {
        val code = """
            class NotEnum {
                companion object {
                    fun values(): Array<NotEnum> = emptyArray()
                }
            }

            fun foo() {
                NotEnum.values()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a values call through an import alias passes`() {
        val code = """
            import EnumClass.values as valuesAlias

            private enum class EnumClass

            fun foo() {
                valuesAlias()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a values function reference passes`() {
        val code = """
            enum class EnumClass

            fun foo() {
                EnumClass::values
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an entries access passes`() {
        val code = """
            enum class EnumClass

            fun foo() {
                EnumClass.entries
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
