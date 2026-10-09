package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinMisorderedAssertEqualsArgumentsTest {

    private val environment = createEnvironment()

    private val sut = KotlinMisorderedAssertEqualsArguments(Config.empty)

    private val testCase = """
        package junit.framework

        open class TestCase {
            fun assertEquals(expected: Any?, actual: Any?) {}
            fun assertEquals(message: String, expected: Any?, actual: Any?) {}
            fun assertSame(expected: Any?, actual: Any?) {}
        }
    """.trimIndent()

    private val testNg = """
        package org.testng

        object Assert {
            fun assertEquals(actual: Any?, expected: Any?) {}
        }
    """.trimIndent()

    @Test
    fun `a string literal passed as the actual argument is reported`() {
        val code = """
            import junit.framework.TestCase

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(buildTags: String) {
                    assertEquals(buildTags, "release-keys")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enum constant passed as the actual argument is reported`() {
        val code = """
            import junit.framework.TestCase

            enum class Abi { X86_ABI, ARM64_V8A_ABI }

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(cpuAbi: Abi) {
                    assertSame(cpuAbi, Abi.X86_ABI)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a list of literals passed as the actual argument is reported`() {
        val code = """
            import junit.framework.TestCase

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(characteristics: List<String>) {
                    assertEquals(characteristics, listOf("emulator", "watch"))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a list of constant local variables passed as the actual argument is reported`() {
        val code = """
            import junit.framework.TestCase

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(actual: List<Int>) {
                    val one = 1
                    assertEquals(actual, listOf(one, one))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a static factory call passed as the actual argument is reported`() {
        val code = """
            import junit.framework.TestCase

            object AnonymizerUtil {
                fun anonymizeUtf8(value: String): String = value
            }

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(serialNumber: String) {
                    assertEquals("message", serialNumber, AnonymizerUtil.anonymizeUtf8("serial"))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a data class instance passed as the actual argument is reported`() {
        val code = """
            import junit.framework.TestCase

            data class Point(val x: Int, val y: Int)

            class PointTest : TestCase() {
                fun testPoint(point: Point) {
                    assertEquals(point, Point(1, 2))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a static final Java field passed as the actual argument is reported`() {
        val code = """
            import java.math.BigDecimal
            import junit.framework.TestCase

            class AmountTest : TestCase() {
                fun testAmount(amount: BigDecimal) {
                    assertEquals(amount, BigDecimal.ZERO)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal passed as the expected argument of a TestNG assertion is reported`() {
        val code = """
            import org.testng.Assert

            class DeviceInfoTest {
                fun testDeviceInfo(buildTags: String) {
                    Assert.assertEquals("release-keys", buildTags)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testNg)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal passed as the actual argument of a kotlin test assertion is reported`() {
        val code = """
            import kotlin.test.assertEquals

            fun test(buildTags: String) {
                assertEquals(buildTags, "release-keys")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal passed as the expected argument passes`() {
        val code = """
            import junit.framework.TestCase

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(buildTags: String) {
                    assertEquals("release-keys", buildTags)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(0, findings.size)
    }

    @Test
    fun `parameters named expected and actual pass`() {
        val code = """
            import junit.framework.TestCase

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(expected: String, actual: String) {
                    assertEquals(expected, actual)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(0, findings.size)
    }

    @Test
    fun `named arguments pass`() {
        val code = """
            import junit.framework.TestCase

            class DeviceInfoTest : TestCase() {
                fun testDeviceInfo(buildTags: String) {
                    assertEquals(actual = buildTags, expected = "release-keys")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a TestNG assertion with the literal as the actual argument passes`() {
        val code = """
            import org.testng.Assert

            class DeviceInfoTest {
                fun testDeviceInfo(buildTags: String) {
                    Assert.assertEquals(buildTags, "release-keys")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testNg)

        assertEquals(0, findings.size)
    }

    @Test
    fun `constant conversions on both sides pass`() {
        val code = """
            import junit.framework.TestCase

            object Json {
                fun decodeFromString(value: String): UByte = value.toUByte()
            }

            class JsonTest : TestCase() {
                fun testDecode() {
                    assertEquals(42u.toUByte(), Json.decodeFromString("42"))
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, testCase)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertion function of another owner passes`() {
        val code = """
            fun assertEquals(expected: Any?, actual: Any?) {}

            fun test(buildTags: String) {
                assertEquals(buildTags, "release-keys")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
