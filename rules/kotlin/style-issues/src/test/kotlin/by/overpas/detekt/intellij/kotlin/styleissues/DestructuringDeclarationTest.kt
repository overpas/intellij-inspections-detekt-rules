package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class DestructuringDeclarationTest {

    private val environment = createEnvironment()

    private val sut = DestructuringDeclaration(Config.empty)

    @Test
    fun `a lambda parameter with two used components is reported`() {
        val code = """
            data class XY(val x: String, val y: String)

            fun foo(list: List<XY>) = list.map { xy -> xy.x + xy.y }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit it parameter with two used components is reported`() {
        val code = """
            data class XY(val x: String, val y: String)

            fun convert(xy: XY, foo: (XY) -> String) = foo(xy)

            fun foo(xy: XY) = convert(xy) { it.x + it.y }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `components read into local variables are reported`() {
        val code = """
            data class XY(val x: Int, val y: Int)

            fun test(xys: Array<XY>) {
                xys.forEach { xy ->
                    val x = xy.x
                    println(x)
                    val y = xy.y + x
                    println(y)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an it parameter destructured inside the lambda is reported`() {
        val code = """
            data class XY(val x: String, val y: String)

            fun convert(xy: XY, foo: (XY) -> String) = foo(xy)

            fun foo(xy: XY) = convert(xy) {
                val (x, y) = it
                x + y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a second lambda parameter is reported`() {
        val code = """
            data class XY(val x: String, val y: String)

            fun foo(list: List<XY>) = list.fold("") { prev, xy -> prev + xy.x + xy.y }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map entry lambda parameter is reported`() {
        val code = """
            fun foo(f: (Map.Entry<Int, Int>) -> Int) = f

            fun bar() {
                foo { it.key + it.value }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map entry loop variable is reported`() {
        val code = """
            fun foo(map: Map<String, Int>) {
                for (entry in map.entries) {
                    println(entry.key + entry.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a local variable with two used components is reported`() {
        val code = """
            data class XY(val x: Int, val y: Int)

            fun create() = XY(1, 2)

            fun use(): Int {
                var xy = create()
                return xy.x + xy.y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a pair local variable is reported`() {
        val code = """
            fun foo() = 0 to 10

            fun bar(): Int {
                val b = foo()
                return b.first + b.second
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to a function with the same name is reported`() {
        val code = """
            data class D(val v1: Int, val v2: Int)

            fun foo(f: (D) -> Int) = f

            fun bar() {}

            fun test() {
                foo { bar ->
                    bar()
                    bar.v1 + bar.v2
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the last two of three components are reported`() {
        val code = """
            data class My(val first: String, val second: Int, val third: Boolean)

            fun foo(list: List<My>) {
                list.forEach { my ->
                    println(my.second)
                    println(my.third)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a single used component passes`() {
        val code = """
            data class My(val first: String, val second: Int)

            fun foo(list: List<My>) {
                list.forEach { my ->
                    println(my.second)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a nullable lambda parameter passes`() {
        val code = """
            data class XY(val x: String, val y: String)

            fun test(xys: Array<XY?>) {
                xys.forEach { xy ->
                    println(xy?.x + xy?.y)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reassigned local variable passes`() {
        val code = """
            data class XY(val x: Int, val y: Int)

            fun create() = XY(1, 2)

            fun use(): Int {
                var xy = create()
                xy = create()
                return xy.x + xy.y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a class property passes`() {
        val code = """
            data class XY(val x: Int, val y: Int)

            class Foo {
                val xy = XY(1, 2)
                val sum = xy.x + xy.y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable passed as an argument passes`() {
        val code = """
            data class XY(val x: Int, val y: Int)

            fun consume(xy: XY) = xy

            fun use(xy: XY) {
                listOf(xy).forEach {
                    println(it.x + it.y)
                    consume(it)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a modified component passes`() {
        val code = """
            data class XY(var x: Int, val y: Int)

            fun use(list: List<XY>) {
                list.forEach {
                    it.x += it.y
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `too many unused components pass`() {
        val code = """
            data class Five(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int)

            fun use(list: List<Five>) = list.map { it.a + it.e }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a regular class passes`() {
        val code = """
            class XY(val x: Int, val y: Int)

            fun foo(list: List<XY>) = list.map { it.x + it.y }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
