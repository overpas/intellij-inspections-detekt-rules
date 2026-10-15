package by.overpas.detekt.intellij.jvm.testframeworks.assertbetweeninconvertibletypes

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class AssertBetweenInconvertibleTypesTest {

    private val environment = createEnvironment()

    private val sut = AssertBetweenInconvertibleTypes(Config.empty)

    @Test
    fun `a junit 4 assertEquals between a string and a double is reported`() {
        val code = """
            import org.junit.Assert

            class FooTest {
                fun test() {
                    Assert.assertEquals("hello", 1.0)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a junit 4 assertEquals with a message between inconvertible types is reported`() {
        val code = """
            import org.junit.Assert.assertEquals

            class FooTest {
                fun test() {
                    assertEquals("message", "hello", 1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a junit 5 assertEquals between an int and a string is reported`() {
        val code = """
            import org.junit.jupiter.api.Assertions.assertEquals

            class FooTest {
                fun test() {
                    assertEquals(1, "", "error message")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a junit 5 assertNotEquals between inconvertible types is reported`() {
        val code = """
            import org.junit.jupiter.api.Assertions.assertNotEquals

            class FooTest {
                fun test() {
                    assertNotEquals(intArrayOf(0), 1.0, "message")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a junit 4 assertNotSame between inconvertible types is reported`() {
        val code = """
            import org.junit.Assert.assertNotSame

            class FooTest {
                fun test() {
                    assertNotSame("hello", 1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a testng assertSame between inconvertible types is reported`() {
        val code = """
            import org.testng.Assert.assertSame

            class FooTest {
                fun test() {
                    assertSame("foo", 2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a junit 3 assertEquals in a test case between inconvertible types is reported`() {
        val code = """
            import junit.framework.TestCase

            class FooTest : TestCase() {
                fun testFoo() {
                    assertEquals("foo", 2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two unrelated classes are reported`() {
        val code = """
            import org.junit.Assert.assertEquals

            open class A
            open class B

            class FooTest {
                fun test(a: A, b: B) {
                    assertEquals(a, b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final class and an unrelated interface are reported`() {
        val code = """
            import org.junit.Assert.assertEquals

            interface A
            class B

            class FooTest {
                fun test(a: A, b: B) {
                    assertEquals(a, b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an open class and an unrelated interface pass`() {
        val code = """
            import org.junit.Assert.assertEquals

            interface A
            open class B

            class FooTest {
                fun test(a: A, b: B) {
                    assertEquals(a, b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `two unrelated interfaces pass`() {
        val code = """
            import org.junit.Assert.assertEquals

            interface A
            interface B

            class FooTest {
                fun test(a: A, b: B) {
                    assertEquals(a, b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a subclass and its superclass pass`() {
        val code = """
            import org.junit.Assert.assertEquals

            open class A
            class B : A()

            class FooTest {
                fun test(a: A, b: B) {
                    assertEquals(a, b)
                    assertEquals(b, a)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `collections with different type arguments pass`() {
        val code = """
            import org.junit.Assert.assertEquals

            interface A
            interface B : A

            class FooTest {
                fun test(c1: Collection<A>?, c2: Collection<B>?) {
                    assertEquals(c1, c2)
                    assertEquals(object : ArrayList<String>() {}, ArrayList<String>())
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null argument passes`() {
        val code = """
            import org.junit.Assert.assertEquals

            class FooTest {
                fun test() {
                    assertEquals(null, "foo")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a nothing argument passes`() {
        val code = """
            import org.junit.Assert.assertEquals

            class FooTest {
                fun test() {
                    assertEquals(TODO(), "foo")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `nullable values of the same type pass`() {
        val code = """
            import org.junit.Assert.assertEquals

            class FooTest {
                fun test(a: String?, b: String) {
                    assertEquals(a, b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertEquals with double parameters passes`() {
        val code = """
            import org.junit.Assert.assertEquals

            class FooTest {
                fun test(d: Double) {
                    assertEquals(1.0, d, 0.0)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a kotlin test assertEquals passes`() {
        val code = """
            import kotlin.test.assertEquals

            class FooTest {
                fun test() {
                    assertEquals<Any>("hello", 1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertEquals from another class passes`() {
        val code = """
            object Assert {
                fun assertEquals(expected: Any, actual: Any) {}
            }

            class FooTest {
                fun test() {
                    Assert.assertEquals("hello", 1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertj isEqualTo between a string and an int is reported`() {
        val code = """
            import org.assertj.core.api.Assertions

            class FooTest {
                fun test() {
                    Assertions.assertThat("foo").isEqualTo(2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertj isEqualTo after a description is reported`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat("foo").describedAs("foo").isEqualTo(2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertj isSameAs between an int and a string is reported`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat(1).isSameAs("foo")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertj isNotEqualTo between inconvertible types is reported`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat("hello").`as`("test").isNotEqualTo(1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertj isNotSameAs between inconvertible types is reported`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat(intArrayOf(0)).isNotSameAs(1.0)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertj element of a list with a wrong type is reported`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat(listOf(1, 2)).first().isEqualTo("1")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertj isEqualTo between compatible types passes`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat("foo").isEqualTo("foo")
                    assertThat(1).isEqualTo(1)
                    assertThat(listOf(1)).isEqualTo(listOf(1))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertj isEqualTo with null passes`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test() {
                    assertThat("foo").isEqualTo(null)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an isEqualTo without assertThat passes`() {
        val code = """
            class Checker {
                fun isEqualTo(other: Any) {}
            }

            class FooTest {
                fun test() {
                    Checker().isEqualTo(1)
                    isEqualTo(1)
                }

                fun isEqualTo(other: Any) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertj isEqualTo between an iterable and a class that delegates iterable passes`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class Bag(private val values: List<String>) : Iterable<String> by values

            class FooTest {
                fun test(first: Bag, second: Bag) {
                    assertThat(first).isEqualTo(second)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertj isEqualTo between a mutable list and a string is reported`() {
        val code = """
            import org.assertj.core.api.Assertions.assertThat

            class FooTest {
                fun test(values: MutableList<String>) {
                    assertThat(values).isEqualTo("foo")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }
}
