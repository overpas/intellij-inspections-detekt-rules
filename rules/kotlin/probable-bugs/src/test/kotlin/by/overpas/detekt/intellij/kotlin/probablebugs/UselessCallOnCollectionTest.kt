package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UselessCallOnCollectionTest {

    private val environment = createEnvironment()

    private val sut = UselessCallOnCollection(Config.empty)

    @Test
    fun `filterNotNull on a list of non-null elements is reported`() {
        val code = """
            val x = listOf("1").filterNotNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filterNotNull on a sequence of non-null elements is reported`() {
        val code = """
            val x = sequenceOf("1", "2", "3").filterNotNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filterNotNull on a list of nullable elements passes`() {
        val code = """
            val x = listOf("1", null).filterNotNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filterIsInstance of the element type is reported`() {
        val code = """
            val x = listOf("1").filterIsInstance<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filterIsInstance of a supertype of the element type is reported`() {
        val code = """
            val x = sequenceOf(1).filterIsInstance<Any>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filterIsInstance of a subtype of the element type passes`() {
        val code = """
            val x = listOf(true, "1").filterIsInstance<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filterIsInstance of a non-null type on nullable elements passes`() {
        val code = """
            val x = listOf("1", null).filterIsInstance<Any>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filterIsInstance on platform type elements passes`() {
        val code = """
            val x = listOf(System.getProperty("")).filterIsInstance<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filterIsInstance on an in-projected array passes`() {
        val code = """
            inline fun <reified T> test(x: Array<in T>) {
                val y: List<T> = x.filterIsInstance<T>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filter with a true constant is reported`() {
        val code = """
            val someList = listOf("alpha", "beta").filter { true }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filter with a labeled return of a true constant is reported`() {
        val code = """
            val someList = sequenceOf("alpha", "beta").filter { return@filter true }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filter with a false constant is reported`() {
        val code = """
            val someList = listOf("alpha", "beta").filter { false }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `filter with a constant after other statements passes`() {
        val code = """
            val someList = listOf("alpha", "beta").filter {
                if (it.isEmpty()) return@filter false

                true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filter with a non-local return of a constant passes`() {
        val code = """
            fun f(): Boolean {
                val someList = listOf("alpha", "beta").filter { return@f true }
                return someList.isEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mapNotNull with a lambda that returns non-null values is reported`() {
        val code = """
            val x = listOf("1").mapNotNull { it.toInt() }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `mapNotNull with a reference to a function that returns non-null values is reported`() {
        val code = """
            val x = sequenceOf("1").mapNotNull(String::toInt)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `mapIndexedNotNullTo with a lambda that returns non-null values is reported`() {
        val code = """
            val x = listOf("alpha", "beta").mapIndexedNotNullTo(destination = hashSetOf()) { index, value -> index + value.length }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `mapNotNull with a labeled lambda that returns non-null values is reported`() {
        val code = """
            fun foo(c: Collection<String>) {
                c.mapNotNull label@{
                    return@label ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `mapNotNull with a lambda that can return null passes`() {
        val code = """
            val x = listOf("1").mapNotNull { if (it.isNotEmpty()) it.toInt() else null }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mapNotNull with a reference to a function that can return null passes`() {
        val code = """
            fun String.toNullableInt(): Int? = null

            val x = listOf("1").mapNotNull(String::toNullableInt)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mapNotNull with a lambda that returns a platform type passes`() {
        val code = """
            val x = listOf("1").mapNotNull { java.lang.String.valueOf(it) }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mapNotNull that uses the label of the simpler call passes`() {
        val code = """
            val a = listOf(1, 2, 3).map {
                listOf(1, 2, 3).mapNotNull {
                    if (it == 2) return@map 1
                    it
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mapNotNull imported under another name passes`() {
        val code = """
            import kotlin.collections.map as mapNotNull

            val x = listOf("1").mapNotNull { it.toInt() }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local mapNotNull function passes`() {
        val code = """
            fun List<String>.mapNotNull(f: (String) -> Int) = Unit

            val x = listOf("1").mapNotNull { it.toInt() }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
