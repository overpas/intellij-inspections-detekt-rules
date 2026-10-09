package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConflictingExtensionPropertyTest {

    private val environment = createEnvironment()

    private val sut = ConflictingExtensionProperty(Config.empty)

    @Test
    fun `an extension property that calls the Java getter is reported`() {
        val code = """
            import java.io.File

            val File.name: String
                get() = getName()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension property with another type than the synthetic property is reported`() {
        val code = """
            import java.io.File

            val File.parent: File
                get() = getParentFile()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension property on a Kotlin subclass of a Java class is reported`() {
        val code = """
            import java.io.File

            class MyFile : File("")

            val MyFile.isFile: Boolean
                get() = isFile()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension property on a Java interface without the getter passes`() {
        val code = """
            import java.io.Serializable

            val Serializable.name: String
                get() = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a hidden extension property passes`() {
        val code = """
            @Deprecated("hidden", level = DeprecationLevel.HIDDEN)
            val Thread.priority: Int
                get() = getPriority()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension property on a Kotlin class passes`() {
        val code = """
            class Box(val size: Int)

            val Box.name: String
                get() = "box"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension property with a name that has no Java getter passes`() {
        val code = """
            import java.io.File

            val File.label: String
                get() = getName()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
