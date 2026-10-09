package by.overpas.detekt.intellij.kotlin.logging

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinLoggerInitializedWithForeignClassTest {

    private val environment = createEnvironment()

    private val sut = KotlinLoggerInitializedWithForeignClass(Config.empty)

    @Test
    fun `a java util logger with a foreign qualified name is reported`() {
        val code = """
            import java.util.logging.Logger

            class Foo {
                private val logger = Logger.getLogger(Bar::class.qualifiedName)
            }

            class Bar
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an imported getLogger call with a foreign class is reported`() {
        val code = """
            import java.util.logging.Logger.getLogger

            class Foo {
                private val logger = getLogger(Bar::class.java.name)
            }

            class Bar
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an slf4j logger with a foreign java class is reported`() {
        val code = """
            import org.slf4j.LoggerFactory

            class Foo {
                private val logger = LoggerFactory.getLogger(Bar::class.java)
            }

            class Bar
        """.trimIndent()
        val dependency = """
            package org.slf4j

            object LoggerFactory {
                fun getLogger(clazz: Class<*>): Any = clazz
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a log4j2 logger with a foreign java class is reported`() {
        val code = """
            import org.apache.logging.log4j.LogManager

            class Foo {
                private val logger = LogManager.getLogger(Bar::class.java)
            }

            class Bar
        """.trimIndent()
        val dependency = """
            package org.apache.logging.log4j

            object LogManager {
                fun getLogger(clazz: Class<*>): Any = clazz
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a commons logger with a foreign canonical name getter is reported`() {
        val code = """
            import org.apache.commons.logging.LogFactory

            class Foo {
                private val logger = LogFactory.getLog(Bar::class.java.getCanonicalName())
            }

            class Bar
        """.trimIndent()
        val dependency = """
            package org.apache.commons.logging

            object LogFactory {
                fun getLog(clazz: Class<*>): Any = clazz

                fun getLog(name: String?): Any = name.orEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a commons logger with a foreign simple name is reported`() {
        val code = """
            import org.apache.commons.logging.LogFactory

            class Foo {
                private val logger = LogFactory.getLog(Bar::class.simpleName)
            }

            class Bar
        """.trimIndent()
        val dependency = """
            package org.apache.commons.logging

            object LogFactory {
                fun getLog(clazz: Class<*>): Any = clazz

                fun getLog(name: String?): Any = name.orEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a logger in a companion object with a foreign class is reported`() {
        val code = """
            import java.util.logging.Logger

            class A

            class B {
                companion object C {
                    val logger = Logger.getLogger(A::class.java.name)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a logger in a nested class with the outer class is reported`() {
        val code = """
            import java.util.logging.Logger

            class A {
                class B {
                    val logger = Logger.getLogger(A::class.java.name)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a logger in an object with a foreign class is reported`() {
        val code = """
            import java.util.logging.Logger

            class A

            object B {
                val logger = Logger.getLogger(A::class.java.name)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a logger with the own class passes`() {
        val code = """
            import org.apache.commons.logging.LogFactory

            class Foo {
                private val logger = LogFactory.getLog(Foo::class.java)
            }
        """.trimIndent()
        val dependency = """
            package org.apache.commons.logging

            object LogFactory {
                fun getLog(clazz: Class<*>): Any = clazz

                fun getLog(name: String?): Any = name.orEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a logger in a companion object with the outer class passes`() {
        val code = """
            import java.util.logging.Logger

            class B {
                companion object {
                    val logger = Logger.getLogger(B::class.java.name)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a logger in a companion object with the companion class passes`() {
        val code = """
            import java.util.logging.Logger

            class B {
                companion object {
                    val logger = Logger.getLogger(Companion::class.java.name)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a logger in a named companion object with the companion class passes`() {
        val code = """
            import java.util.logging.Logger

            class B {
                companion object C {
                    val logger = Logger.getLogger(C::class.java.name)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a logger outside of a class passes`() {
        val code = """
            import java.util.logging.Logger

            class Bar

            val logger: Logger = Logger.getLogger(Bar::class.java.name)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function that is not a logger factory passes`() {
        val code = """
            class Foo {
                private val logger = getLogger(Bar::class.java)
            }

            class Bar

            fun getLogger(clazz: Class<*>): Any = clazz
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
