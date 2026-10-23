package by.overpas.detekt.intellij.android.kotlin.illegalidentifier

import dev.detekt.api.Config
import dev.detekt.test.lint
import dev.detekt.test.utils.compileContentForTest
import dev.detekt.test.utils.compileForTest
import kotlin.io.path.createParentDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IllegalIdentifierTest {

    private val sut = IllegalIdentifier(Config.empty)

    private val dir = createTempDirectory()

    @AfterTest
    fun deleteDir() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun `a backticked function name with a space is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val expected = listOf("`load the data`")

        val findings = sut.lint(code)
        val reportedTexts = findings.map { finding ->
            finding.entity.location.text.run { code.substring(start, end) }
        }

        assertEquals(expected, reportedTexts)
    }

    @Test
    fun `each backticked identifier with an illegal character is reported`() {
        val code = """
            fun `load the data`() {}

            fun call() {
                `load the data`()
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a backticked property name with a dot is reported`() {
        val code = """
            val `user.name` = ""
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backticked keyword passes`() {
        val code = """
            val `in` = 1
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name with a dash passes`() {
        val code = """
            val `user-name` = ""
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name with non latin letters passes`() {
        val code = """
            val `имя` = ""
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an illegal character inside a string passes`() {
        val code = """
            val name = "`load the data`"
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name inside kdoc passes`() {
        val code = """
            /**
             * @sample `foo bar`
             */
            fun f() {}
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a kotlin script passes`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val script = compileContentForTest(code, "build.gradle.kts")

        val findings = sut.lint(script)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name in a desktop test source set passes`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/desktopTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name in a custom host test source set passes`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/androidTvTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name in a shared jvm test source set passes`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/jvmSharedTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name in a unit test source set passes`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/testDebug/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name in a screenshot test source set passes`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/screenshotTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backticked name in an android test source set is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/androidTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backticked name in an android instrumented test source set is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/androidInstrumentedTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backticked name in an android device test source set is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/androidTvDeviceTest/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backticked name in a main source set is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/androidMain/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backticked name in a main package named test is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/main/kotlin/test/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backticked name in a source set with test inside a word is reported`() {
        val code = """
            fun `load the data`() {}
        """.trimIndent()
        val path = dir.resolve("src/attestation/kotlin/A.kt").createParentDirectories()
        path.writeText(code)
        val file = compileForTest(path)

        val findings = sut.lint(file)

        assertEquals(1, findings.size)
    }
}
