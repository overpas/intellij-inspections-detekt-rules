package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceJavaStaticMethodWithKotlinAnalogTest {

    private val environment = createEnvironment()

    private val sut = ReplaceJavaStaticMethodWithKotlinAnalog(Config.empty)

    @Test
    fun `Math functions with a Kotlin counterpart are reported`() {
        val code = """
            fun test(x: Double, y: Double, i: Int) {
                Math.abs(x)
                Math.max(x, y)
                Math.sqrt(x)
                Math.abs(i).let { println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `Math functions that become extensions are reported`() {
        val code = """
            fun test(x: Double, y: Double) {
                Math.pow(x, y)
                Math.round(x)
                Math.copySign(x, y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `a System exit call is reported`() {
        val code = """
            fun test() {
                System.exit(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `print calls on System out are reported`() {
        val code = """
            import java.lang.System.out

            fun test() {
                System.out.println("foo")
                listOf("").forEach { out.print(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a print call on System err passes`() {
        val code = """
            fun test() {
                System.err.print("foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `primitive toString calls are reported`() {
        val code = """
            fun test(number: Int, list: List<Int>) {
                Integer.toString(number, 2)
                Integer.toString(list[0])
                java.lang.Long.toString(5L)
                java.lang.Character.toString('3')
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `a toString call with an invalid literal radix passes`() {
        val code = """
            fun test() {
                Integer.toString(42, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a Character toString call with a code point passes`() {
        val code = """
            fun test(codePoint: Int) {
                java.lang.Character.toString(codePoint)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a primitive compare call is reported`() {
        val code = """
            fun test() {
                Integer.compare(5, 6)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `Arrays functions are reported`() {
        val code = """
            import java.util.Arrays

            fun test(a: Array<*>?, b: Array<Int>) {
                java.util.Arrays.copyOf(intArrayOf(1, 2, 3), 3)
                Arrays.toString(a)
                Arrays.deepEquals(b, a)
                Arrays.equals(b, b)
                Arrays.asList(1, 3, null)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(5, findings.size)
    }

    @Test
    fun `Arrays functions without a Kotlin counterpart pass`() {
        val code = """
            import java.util.Arrays

            fun test(a: Array<Int>, b: Array<Int>) {
                Arrays.equals(a, 1, 2, b, 1, 2)
                Arrays.copyOf(a, 3, Array<Number>::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copyOf function of another class passes`() {
        val code = """
            class A {
                fun copyOf(x: Int, y: Int) {
                }
            }

            fun test() {
                A().copyOf(1, 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a Set of call is reported`() {
        val code = """
            fun test() {
                java.util.Set.of("a", "b")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `Collections functions on mutable lists are reported`() {
        val code = """
            import java.util.Collections
            import java.util.LinkedList

            fun test(linked: LinkedList<String>) {
                val list = mutableListOf(1, 2)
                Collections.reverse(list)
                Collections.fill(list, 3)
                Collections.shuffle(list)
                Collections.sort(linked)
                Collections.sort(list, { a, b -> a.compareTo(b) })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(5, findings.size)
    }

    @Test
    fun `Collections functions on read-only lists pass`() {
        val code = """
            import java.util.Collections

            fun test() {
                val list = listOf(1, 2)
                Collections.reverse(list)
                Collections.shuffle(list)
                Collections.sort(list)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
