package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaMapForEachTest {

    private val environment = createEnvironment()

    private val sut = JavaMapForEach(Config.empty)

    @Test
    fun `a Java forEach call on a map is reported`() {
        val code = """
            fun test(map: Map<Int, String>) {
                map.forEach { key, value ->
                    foo(key, value)
                }
            }

            fun foo(i: Int, s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Java forEach call on a hash map is reported`() {
        val code = """
            fun test(hashMap: HashMap<Int, String>) {
                hashMap.forEach { key, value ->
                    foo(key, value)
                }
            }

            fun foo(i: Int, s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Java forEach call with the lambda in parentheses is reported`() {
        val code = """
            fun test(map: Map<Int, String>) {
                map.forEach({ key, value -> foo(key, value) })
            }

            fun foo(i: Int, s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Java forEach call on an implicit map receiver is reported`() {
        val code = """
            fun test(map: Map<Int, String>) {
                map.run {
                    forEach { key, value ->
                        foo(key, value)
                    }
                }
            }

            fun foo(i: Int, s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Kotlin forEach call with a destructured entry passes`() {
        val code = """
            fun test(map: Map<Int, String>) {
                map.forEach { (key, value) ->
                    foo(key, value)
                }
            }

            fun foo(i: Int, s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a Kotlin forEach call with an implicit entry parameter passes`() {
        val code = """
            fun test(map: Map<Int, String>) {
                map.forEach {
                    foo(it)
                }
            }

            fun foo(entry: Map.Entry<Int, String>) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach call with a destructured key parameter passes`() {
        val code = """
            fun test() {
                val mapOfPairs = mapOf<Pair<Int, Int>, Int>()
                mapOfPairs.forEach { (first, second), value ->
                    println(first + second + value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach call with more than one argument passes`() {
        val code = """
            import java.util.concurrent.ConcurrentHashMap

            fun test(map: ConcurrentHashMap<Int, String>) {
                map.forEach(1) { key, value ->
                    foo(key, value)
                }
            }

            fun foo(i: Int, s: String) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a two-parameter forEach on a non-map receiver passes`() {
        val code = """
            class Pairs {
                fun forEach(action: (Int, Int) -> Unit) {
                    action(1, 2)
                }
            }

            fun test(pairs: Pairs) {
                pairs.forEach { first, second ->
                    println(first + second)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
