package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class LiftReturnOrAssignmentTest {

    private val environment = createEnvironment()

    private val sut = LiftReturnOrAssignment(Config.empty)

    @Test
    fun `returns in both branches of an if are reported`() {
        val code = """
            fun test(n: Int): String {
                if (n == 1)
                    return "one"
                else
                    return "two"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in an if else-if chain with else are reported`() {
        val code = """
            fun test(n: Int): String {
                if (n == 1)
                    return "one"
                else if (n == 2)
                    return "two"
                else
                    return "three"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in all when branches are reported`() {
        val code = """
            fun test(n: Int): String {
                when (n) {
                    1 -> return "one"
                    else -> return "two"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in an exhaustive when over an enum are reported`() {
        val code = """
            enum class TestEnum { A, B, C }

            fun test(e: TestEnum): Int {
                when (e) {
                    TestEnum.A -> return 1
                    TestEnum.B -> return 2
                    TestEnum.C -> return 3
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in a when with jumping branches are reported`() {
        val code = """
            fun foo(): Int {
                loop@ while (true) {
                    when (1) {
                        1 -> return 1
                        2 -> throw Exception()
                        3 -> break@loop
                        4 -> continue@loop
                        else -> return -1
                    }
                }
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in a try and its catch are reported`() {
        val code = """
            fun doSomething() {}

            fun test(): String {
                try {
                    return "success"
                } catch (e: Exception) {
                    return "failure"
                } finally {
                    doSomething()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `assignments in both branches of an if are reported`() {
        val code = """
            fun test(n: Int): String {
                var res: String
                if (n == 1) res = "one" else res = "two"
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `augmented assignments in both branches of an if are reported`() {
        val code = """
            fun test(n: Int): String {
                var res = "!"
                if (n == 1) res += "one" else res += "two"
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `assignments in all when branches are reported`() {
        val code = """
            fun test(n: Int): String {
                val res: String
                when (n) {
                    1 -> res = "one"
                    else -> res = "two"
                }
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `assignments of null and a nullable value are reported`() {
        val code = """
            fun bar(i: Int, x: String?): String? {
                var str: String? = null
                if (i == 1) {
                    str = null
                } else if (i == 2) {
                    str = "2"
                } else {
                    str = x
                }
                return str
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `assignments of subtypes of the variable type are reported`() {
        val code = """
            open class A

            class B : A()

            class C : A()

            fun liftClass(boolean: Boolean): A {
                val a1: A
                if (boolean) {
                    a1 = B()
                } else {
                    a1 = C()
                }
                return a1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `plusAssign calls of the same operator are reported`() {
        val code = """
            fun test(b: Boolean, x: Int, y: Int?): List<Int?> {
                val list = mutableListOf<Int?>()
                if (b) {
                    list += x
                } else {
                    list += y
                }
                return list
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `assignments in a try and its catch with an unrelated finally assignment are reported`() {
        val code = """
            fun test(): String? {
                var res: String? = null
                var foo: String? = null
                try {
                    res = "success"
                } catch (e: Exception) {
                    res = "failure"
                } finally {
                    foo = "finally"
                }
                return res + foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in nested if and when branches are reported for each expression`() {
        val code = """
            fun test(x: Any): String {
                if (x is String)
                    when {
                        x.length > 3 -> return "long string"
                        else -> return "short string"
                    }
                else if (x is Int)
                    when {
                        x > 999 -> return "long int"
                        else -> return "short int"
                    }
                else if (x is Long)
                    TODO()
                else
                    return "I don't know"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `assignments in a when without else over an int pass`() {
        val code = """
            fun test(n: Int): Int {
                var res = 0
                when (n) {
                    1 -> res = 1
                    2 -> res = 2
                }
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if without else passes`() {
        val code = """
            fun test(n: Int): String {
                if (n == 1)
                    return "one"
                else if (n == 2)
                    return "two"
                return "three"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `returns after other statements pass`() {
        val code = """
            fun doSomething() {}

            fun test(n: Int): String {
                if (n == 1) {
                    doSomething()
                    return "one"
                } else {
                    doSomething()
                    return "two"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single return with a throwing branch passes`() {
        val code = """
            fun foo(arg: Int): Int {
                when (arg) {
                    0 -> return 0
                    else -> throw Exception()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when with another return inside passes`() {
        val code = $$"""
            fun test(n: Int, arg: String?): String {
                when (n) {
                    1 -> {
                        if (arg == null) return ""
                        return "** $arg"
                    }
                    else -> {
                        return "Strange"
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `returned lambdas pass`() {
        val code = """
            fun foo(x: Boolean): (Int) -> String {
                when (x) {
                    true -> return { it.toString() }
                    else -> return { "42" }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled return passes`() {
        val code = """
            fun test(arg: String?): Int {
                arg?.let {
                    when (arg) {
                        "" -> return 1
                        else -> return@let 42
                    }
                }
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if longer than fifteen lines passes`() {
        val code = """
            fun log(s: String) {}

            fun test(n: Int): String {
                if (n == 1) {
                    log("1")
                    log("2")
                    log("3")
                    log("4")
                    log("5")
                    log("6")
                    return "one"
                } else {
                    log("1")
                    log("2")
                    log("3")
                    log("4")
                    log("5")
                    log("6")
                    log("7")
                    return "two"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if used as an expression passes`() {
        val code = """
            var a = 5

            fun foo() = if (a > 0) {
                a = 6
            } else {
                a = 8
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `assignments to different variables pass`() {
        val code = """
            fun test(n: Int): String {
                var res = ""
                var res2 = ""
                if (n == 1) {
                    res = "one"
                } else {
                    res2 = "two"
                }
                return res + res2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `assignments to a shadowing variable pass`() {
        val code = """
            fun test(n: Int): String {
                var res = ""
                if (n == 1) {
                    res = "one"
                } else {
                    var res: String
                    res = "two"
                }
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `different augmented assignment operators pass`() {
        val code = """
            fun test(s: String): Int {
                var n = 1
                if (s == "add") {
                    n += 1
                } else {
                    n -= 1
                }
                return n
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `augmented assignments with different operand types pass`() {
        val code = """
            fun test(b: Boolean, x: Long, y: Int): Long {
                var num = 0L
                if (b) {
                    num += x
                } else {
                    num += y
                }
                return num
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `augmented assignments to properties of different classes pass`() {
        val code = """
            class A {
                val list: MutableList<String> = mutableListOf()
            }

            class B {
                val list: MutableList<String> = mutableListOf()
            }

            fun Any.add(s: String) {
                when (this) {
                    is A -> list += s
                    is B -> list += s
                    else -> throw IllegalStateException()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a matching assignment in finally passes`() {
        val code = """
            fun test(): String? {
                var res: String? = null
                try {
                    res = "success"
                } catch (e: Exception) {
                    res = "failure"
                } finally {
                    res = "finally"
                }
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assignment followed by another statement passes`() {
        val code = """
            fun doSomething() {}

            fun test(): String? {
                var res: String? = null
                try {
                    res = "success"
                } catch (e: Exception) {
                    res = "failure"
                    doSomething()
                }
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single assignment with jumping branches passes`() {
        val code = """
            fun foo(): Int {
                var res = 0
                loop@ while (true) {
                    when (1) {
                        1 -> res += 1
                        2 -> throw Exception()
                        3 -> break@loop
                        4 -> continue@loop
                        else -> return -1
                    }
                }
                return res
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
