package by.overpas.detekt.intellij.jvm.testframeworks.testmethodwithoutassertion

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMethodWithoutAssertionTest {

    private val environment = createEnvironment()

    private val sut = TestMethodWithoutAssertion(Config.empty)

    @Test
    fun `an empty junit 3 test method is reported`() {
        val code = """
            import junit.framework.TestCase

            class FooTest : TestCase() {
                fun test() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an empty junit 4 test method is reported`() {
        val code = """
            import org.junit.Test

            class FooTest {
                @Test
                fun test() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an empty junit 5 parameterized test is reported`() {
        val code = """
            import org.junit.jupiter.params.ParameterizedTest
            import org.junit.jupiter.params.provider.ValueSource

            class FooTest {
                @ParameterizedTest
                @ValueSource(ints = [1])
                fun test(x: Int) {
                    println(x)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an empty kotlin test method is reported`() {
        val code = """
            import kotlin.test.Test

            class FooTest {
                @Test
                fun test() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a junit 4 test method with an expected exception passes`() {
        val code = """
            import org.junit.Test

            class FooTest {
                @Test(expected = Exception::class)
                fun test() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a testng test method with expected exceptions passes`() {
        val code = """
            import org.testng.annotations.Test

            class FooTest {
                @Test(expectedExceptions = [Exception::class])
                fun test() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a junit assertion passes`() {
        val code = """
            import junit.framework.TestCase
            import org.junit.Assert

            class FooTest : TestCase() {
                @org.junit.Test
                fun fourOhTest() {
                    Assert.assertTrue(true)
                }

                fun test2() {
                    assertTrue(true)
                }

                fun test3() {
                    fail()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call to a helper with an assertion as the last statement passes`() {
        val code = """
            import org.junit.Assert
            import org.junit.Test

            class FooTest {
                @Test
                fun delegateOnly() {
                    check()
                }

                @Test
                fun delegateAdditionally() {
                    println(9)
                    this.check()
                }

                @Test
                fun delegateExpression() = check()

                private fun check() {
                    Assert.assertTrue(true)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call to a helper with an assertion before the last statement is reported`() {
        val code = """
            import org.junit.Assert
            import org.junit.Test

            class FooTest {
                @Test
                fun test() {
                    check()
                    println()
                }

                private fun check() {
                    Assert.assertTrue(true)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertion in a lambda passes`() {
        val code = """
            import org.junit.jupiter.api.Assertions.assertEquals
            import org.junit.jupiter.api.Test

            class FooTest {
                @Test
                fun test() {
                    listOf(1).forEach { assertEquals(1, it) }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an object literal of a verification class passes`() {
        val code = """
            import mockit.Verifications
            import org.junit.Test

            class FooTest {
                @Test
                fun test() {
                    object : Verifications() {}
                }
            }
        """.trimIndent()
        val dependency = """
            package mockit

            abstract class Verifications protected constructor()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a kotlin assert passes`() {
        val code = """
            import org.junit.Test

            class FooTest {
                @Test
                fun test() {
                    assert(true)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a kotlin assert is reported when it does not count as an assertion`() {
        val code = """
            import org.junit.Test

            class FooTest {
                @Test
                fun test() {
                    assert(true)
                }
            }
        """.trimIndent()
        val sut = TestMethodWithoutAssertion(TestConfig("isAssertKeywordAssertion" to false))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `kotlin test and junit 5 kotlin assertions pass`() {
        val code = """
            import org.junit.jupiter.api.Test
            import org.junit.jupiter.api.assertDoesNotThrow
            import kotlin.test.assertTrue

            class FooTest {
                @Test
                fun test1() {
                    assertTrue(true)
                }

                @Test
                fun test2() {
                    assertDoesNotThrow { println() }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `assertj and hamcrest assertions pass`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat
            import org.hamcrest.MatcherAssert
            import org.hamcrest.Matchers.`is`
            import org.junit.jupiter.api.Test

            class FooTest {
                @Test
                fun test1() {
                    assertThat(1).isEqualTo(1)
                }

                @Test
                fun test2() {
                    MatcherAssert.assertThat(1, `is`(1))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a testng assertion passes`() {
        val code = """
            import org.testng.Assert
            import org.testng.annotations.Test

            class FooTest {
                @Test
                fun test() {
                    Assert.assertTrue(true)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a test method with throws is reported by default`() {
        val code = """
            import org.junit.Test

            class FooTest {
                @Test
                @Throws(AssertionError::class)
                fun test() {
                    if (true) throw AssertionError()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a test method with throws passes when exceptions are ignored`() {
        val code = """
            import org.junit.Test

            class FooTest {
                @Test
                @Throws(AssertionError::class)
                fun test() {
                    if (true) throw AssertionError()
                }
            }
        """.trimIndent()
        val sut = TestMethodWithoutAssertion(TestConfig("isThrowingIgnored" to true))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a configured infix assertion passes`() {
        val code = """
            package foo

            import org.junit.Test

            infix fun Int.shouldBe(other: Int) {}

            class FooTest {
                @Test
                fun test() {
                    1 shouldBe 1
                }
            }
        """.trimIndent()
        val sut = TestMethodWithoutAssertion(TestConfig("assertionMethods" to listOf("invalid", "#x", "foo#should.*")))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a configured member assertion passes`() {
        val code = """
            import org.junit.Test

            object Checks {
                fun verifyAll() {}
            }

            class FooTest {
                @Test
                fun test() {
                    Checks.verifyAll()
                }
            }
        """.trimIndent()
        val sut = TestMethodWithoutAssertion(TestConfig("assertionMethods" to listOf("Checks#verify.*")))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function that is not a test passes`() {
        val code = """
            class FooTest {
                fun test() {}
            }

            fun test() {}

            object Bar {
                @org.junit.Test
                fun test() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertion with an unresolved argument passes`() {
        val code = """
            import kotlin.test.Test
            import kotlin.test.assertEquals

            class FooTest {
                @Test
                fun test() {
                    val actual = unresolved()
                    assertEquals(1, actual.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, allowCompilationErrors = true)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a callable reference to an assertion is reported`() {
        val code = """
            import org.junit.Assert
            import org.junit.Test

            class FooTest {
                @Test
                fun test() {
                    true.run(Assert::assertTrue)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }
}
