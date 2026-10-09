package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class VerboseNullabilityAndEmptinessTest {

    private val environment = createEnvironment()

    private val sut = VerboseNullabilityAndEmptiness(Config.empty)

    @Test
    fun `a null check or isEmpty on a list is reported`() {
        val code = """
            fun test(list: List<Int>?) {
                if (list == null || list.isEmpty()) println(0) else println(list.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null check and isNotEmpty on a list is reported`() {
        val code = """
            fun test(list: List<Int>?) {
                if (list != null && list.isNotEmpty()) println(list.size) else println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null check and a negated isEmpty on a list is reported`() {
        val code = """
            fun test(list: List<Int>?) {
                if (list != null && !list.isEmpty()) println(list.size) else println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isBlank on a string is reported`() {
        val code = """
            fun test(str: String?) {
                if (str == null || str.isBlank()) println(0) else println(str.length)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null check and isNotBlank on a string is reported`() {
        val code = """
            fun test(str: String?) {
                if (str != null && str.isNotBlank()) println(str.length) else println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isEmpty on an array is reported`() {
        val code = """
            fun test(array: Array<String>?) {
                if (array == null || array.isEmpty()) println(0) else println(array.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isEmpty on a map is reported`() {
        val code = """
            fun test(map: Map<Int, String>?) {
                if (map == null || map.isEmpty()) println(0) else println(map.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isEmpty on a hash set is reported`() {
        val code = """
            fun test(set: HashSet<Int>?) {
                if (set == null || set.isEmpty()) println(0) else println(set.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isEmpty on a property chain is reported`() {
        val code = """
            class Foo(val bar: Bar)
            class Bar(val list: List<Int>?)

            fun test(foo: Foo) {
                if (foo.bar.list == null || foo.bar.list.isEmpty()) println(0) else println(foo.bar.list.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null check and isNotEmpty inside a longer conjunction is reported`() {
        val code = """
            fun test(list: List<String>?, flag: Boolean, flag2: Boolean) {
                if (flag && list != null && list.isNotEmpty() && flag2) println(list.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check of this or an implicit isEmpty is reported`() {
        val code = """
            fun List<Int>?.test() {
                if (this == null || isEmpty()) println(0) else println(size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check of this or an explicit isEmpty is reported`() {
        val code = """
            fun List<Int>?.test() {
                if (this == null || this.isEmpty()) println(0) else println(size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized null check or isEmpty is reported`() {
        val code = """
            fun test(list: List<Int>?) {
                if ((list == null) || list.isEmpty()) println(1) else println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isEmpty on a mutable property passes`() {
        val code = """
            class Foo(val bar: Bar)
            class Bar(var list: List<Int>?)

            fun test(foo: Foo) {
                if (foo.bar.list == null || foo.bar.list!!.isEmpty()) println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check or isEmpty on a primitive array passes`() {
        val code = """
            fun test(intArray: IntArray?) {
                if (intArray == null || intArray.isEmpty()) println(0) else println(intArray.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a not-null check and isEmpty passes`() {
        val code = """
            fun test(list: List<Int>?) {
                val x = list != null && list.isEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check or isNotEmpty passes`() {
        val code = """
            fun test(list: List<Int>?) {
                val x = list == null || list.isNotEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled null check of this or isEmpty is reported`() {
        val code = """
            fun List<Int>?.test() {
                if (this@test == null || this@test.isEmpty()) println(0) else println(size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or isEmpty wrapped in parentheses inside a conjunction is reported`() {
        val code = """
            fun test(list: List<Int>?, b: Boolean) {
                if ((list == null || list.isEmpty()) && b) println(1) else println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or a negated isNotEmpty on a string is reported`() {
        val code = """
            fun test(str: String?) {
                if (str == null || !str.isNotEmpty()) println(0) else println(str.length)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check and isEmpty with annotated and labeled operands is reported`() {
        val code = """
            @Retention(AnnotationRetention.SOURCE)
            @Target(AnnotationTarget.EXPRESSION)
            annotation class Anno

            fun test(list: List<Int>?) {
                if (((@Anno label@ list) == null) || (@Anno label2@ list).isEmpty()) println(0) else println(list.size)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check or a custom isEmpty passes`() {
        val code = """
            class Comment {
                fun isEmpty(): Boolean = false
            }

            fun test(c: Comment?) {
                if (c == null || c.isEmpty()) println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check or isEmpty of another variable passes`() {
        val code = """
            fun test(list: List<Int>?, other: List<Int>) {
                if (list == null || other.isEmpty()) println(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check or isEmpty inside an isNullOrEmpty implementation passes`() {
        val code = """
            fun Collection<*>?.isNullOrEmpty(): Boolean = this == null || isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
