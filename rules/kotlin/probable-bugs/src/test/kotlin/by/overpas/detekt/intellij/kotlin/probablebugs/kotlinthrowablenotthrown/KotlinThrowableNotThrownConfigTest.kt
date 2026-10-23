package by.overpas.detekt.intellij.kotlin.probablebugs.kotlinthrowablenotthrown

import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinThrowableNotThrownConfigTest {

    private val sut = checkNotNull(javaClass.classLoader.getResource("config/config.yml"))
        .readText()
        .substringAfter("  KotlinThrowableNotThrown:\n")
        .substringAfter("excludes: [")
        .substringBefore("]\n")
        .split(",")
        .map { FileSystems.getDefault().getPathMatcher("glob:${it.trim().removeSurrounding("'")}") }

    @Test
    fun `files in test source sets are excluded by default`() {
        val paths = listOf(
            "app/src/test/kotlin/FooTest.kt",
            "app/src/testDebug/kotlin/FooTest.kt",
            "app/src/androidTest/kotlin/FooTest.kt",
            "lib/src/commonTest/kotlin/FooTest.kt",
            "lib/src/jvmTest/kotlin/FooTest.kt",
            "lib/src/iosTest/kotlin/FooTest.kt",
            "lib/src/androidUnitTest/kotlin/FooTest.kt",
            "lib/src/androidInstrumentedTest/kotlin/FooTest.kt",
            "lib/src/androidUnitTestDebug/kotlin/FooTest.kt",
        )
        val expected = emptyList<String>()

        val included = paths.filterNot { path -> sut.any { it.matches(Path.of(path)) } }

        assertEquals(expected, included)
    }

    @Test
    fun `files in main source sets are not excluded by default`() {
        val paths = listOf(
            "app/src/main/kotlin/Foo.kt",
            "app/src/debug/kotlin/Foo.kt",
            "lib/src/commonMain/kotlin/FooTest.kt",
            "lib/src/jvmMain/kotlin/Foo.kt",
            "lib/src/androidMain/kotlin/Foo.kt",
        )
        val expected = emptyList<String>()

        val excluded = paths.filter { path -> sut.any { it.matches(Path.of(path)) } }

        assertEquals(expected, excluded)
    }
}
