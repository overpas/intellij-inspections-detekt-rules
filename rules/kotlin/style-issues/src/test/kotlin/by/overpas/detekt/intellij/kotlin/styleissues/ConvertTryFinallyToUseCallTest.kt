package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertTryFinallyToUseCallTest {

    private val environment = createEnvironment()

    private val sut = ConvertTryFinallyToUseCall(Config.empty)

    @Test
    fun `a finally block that closes a local reader is reported`() {
        val code = """
            import java.io.File

            fun main() {
                val reader = File("hello-world.txt").bufferedReader()
                try {
                    reader.readLine()
                } finally {
                    reader.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a finally block that closes a parameter is reported`() {
        val code = """
            import java.io.BufferedReader

            fun foo(reader: BufferedReader) {
                try {
                    reader.readLine()
                } finally {
                    reader.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a finally block that closes a nullable resource with a safe call is reported`() {
        val code = """
            import java.io.BufferedReader

            fun foo(reader: BufferedReader?) {
                try {
                    reader?.readLine()
                } finally {
                    reader?.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a finally block that closes the implicit receiver is reported`() {
        val code = """
            import java.io.BufferedReader

            fun BufferedReader.foo() {
                try {
                    readLine()
                } finally {
                    close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a finally block that closes a labeled this is reported`() {
        val code = """
            import java.io.Closeable

            class MyCloseable : Closeable {
                override fun close() {}

                fun process(x: Int) = x

                fun Int.foo() {
                    try {
                        this@MyCloseable.process(this)
                    } finally {
                        this@MyCloseable.close()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a try with a catch clause passes`() {
        val code = """
            import java.io.File
            import java.io.IOException

            fun main() {
                val reader = File("hello-world.txt").bufferedReader()
                try {
                    reader.readLine()
                } catch (e: IOException) {
                    println(e)
                } finally {
                    reader.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a finally block that does not call close passes`() {
        val code = """
            import java.io.File

            fun main() {
                val reader = File("hello-world.txt").bufferedReader()
                try {
                    reader.readLine()
                } finally {
                    reader.readLine()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a finally block with more statements than close passes`() {
        val code = """
            import java.io.File

            fun main() {
                val reader = File("hello-world.txt").bufferedReader()
                try {
                    reader.readLine()
                } finally {
                    reader.readLine()
                    reader.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a close overload with an argument passes`() {
        val code = """
            import java.io.Closeable

            class Resource : Closeable {
                fun doStuff() = Unit
                override fun close() = close(status = 0)
                fun close(status: Int) = println(status)
            }

            fun test() {
                val resource = Resource()
                try {
                    resource.doStuff()
                } finally {
                    resource.close(status = 1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a close overload that hides the Closeable close passes`() {
        val code = """
            import java.io.Closeable

            class Resource : Closeable {
                fun doStuff() = Unit
                @Deprecated(level = DeprecationLevel.HIDDEN, message = "deprecated")
                override fun close() = close(1)
                fun close(status: Int = 0) = println(status)
            }

            fun main() {
                val resource = Resource()
                try {
                    resource.doStuff()
                } finally {
                    resource.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a close function of a class that is not Closeable passes`() {
        val code = """
            class Door {
                fun open() = Unit
                fun close() = Unit
            }

            fun test(door: Door) {
                try {
                    door.open()
                } finally {
                    door.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a close call on a complex receiver passes`() {
        val code = """
            import java.io.BufferedReader

            class Holder(val reader: BufferedReader)

            fun foo(holder: Holder) {
                try {
                    holder.reader.readLine()
                } finally {
                    holder.reader.close()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
