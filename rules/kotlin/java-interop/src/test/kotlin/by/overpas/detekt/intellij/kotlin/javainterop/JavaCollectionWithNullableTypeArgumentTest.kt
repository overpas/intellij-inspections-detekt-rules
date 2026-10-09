package by.overpas.detekt.intellij.kotlin.javainterop

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaCollectionWithNullableTypeArgumentTest {

    private val environment = createEnvironment()

    private val sut = JavaCollectionWithNullableTypeArgument(Config.empty)

    @Test
    fun `a constructor call with a nullable value type is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentHashMap

            val map = ConcurrentHashMap<String, String?>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor call with nullable key and value types is reported once`() {
        val code = """
            import java.util.concurrent.ConcurrentSkipListMap

            val map = ConcurrentSkipListMap<String?, String?>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor call with nullable types in brackets is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentHashMap

            val map = ConcurrentHashMap<(String?), ((String)?)>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor call with an unbounded type parameter is reported`() {
        val code = """
            import java.util.concurrent.ArrayBlockingQueue

            class MyQueue<T> {
                val queue = ArrayBlockingQueue<T>(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor call with a nullable type alias is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentLinkedDeque

            typealias Foo = String?

            val deque = ConcurrentLinkedDeque<Foo>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor call with inferred nullable type arguments is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentHashMap

            val map = mutableMapOf<String?, String?>(null to null)
            val chm = ConcurrentHashMap(map)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor call with expected nullable type arguments is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentHashMap

            val myMap: MutableMap<String?, String?> = ConcurrentHashMap()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type reference with a nullable type argument is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentLinkedQueue

            fun typeUsage() {
                val queue: ConcurrentLinkedQueue<String?>? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return type with a nullable type parameter bound is reported`() {
        val code = """
            import java.util.concurrent.ConcurrentSkipListSet

            interface Source<T : String?> {
                fun nextT(): ConcurrentSkipListSet<T>
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `both a type reference and a constructor call with nullable type arguments are reported`() {
        val code = """
            import java.util.concurrent.ConcurrentLinkedDeque

            typealias Foo = String?

            val deque: ConcurrentLinkedDeque<Foo> = ConcurrentLinkedDeque<Foo>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `non-nullable type arguments pass`() {
        val code = """
            import java.util.concurrent.ArrayBlockingQueue
            import java.util.concurrent.ConcurrentHashMap

            val map = ConcurrentHashMap<String, Int>()
            val queue: ArrayBlockingQueue<Int> = ArrayBlockingQueue<Int>(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter with a non-nullable bound passes`() {
        val code = """
            import java.util.concurrent.ArrayBlockingQueue

            class MyQueue<T : Any> {
                val queue = ArrayBlockingQueue<T>(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a Java collection that supports null passes`() {
        val code = """
            import java.util.HashMap

            val map = HashMap<String?, String?>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a supertype qualifier passes`() {
        val code = """
            interface I {
                fun foo() {}
            }

            class C : I {
                override fun foo() {
                    super<I>.foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
