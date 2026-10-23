package by.overpas.detekt.intellij.kotlin.javainterop.javadefaultmethodsnotoverriddenbydelegation

import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaDefaultMethodsNotOverriddenByDelegationConfigTest {

    private val sut = checkNotNull(javaClass.classLoader.getResource("config/config.yml"))
        .readText()
        .substringAfter("  JavaDefaultMethodsNotOverriddenByDelegation:\n")
        .substringAfter("excludes: [")
        .substringBefore("]\n")
        .split(",")
        .map { FileSystems.getDefault().getPathMatcher("glob:${it.trim().removeSurrounding("'")}") }

    @Test
    fun `files in common source sets are excluded by default`() {
        val paths = listOf(
            "lib/src/commonMain/kotlin/ImmutableEnumMap.kt",
            "lib/src/commonTest/kotlin/ImmutableEnumMapTest.kt",
        )
        val expected = emptyList<String>()

        val included = paths.filterNot { path -> sut.any { it.matches(Path.of(path)) } }

        assertEquals(expected, included)
    }

    @Test
    fun `files in jvm source sets are not excluded by default`() {
        val paths = listOf(
            "app/src/main/kotlin/Foo.kt",
            "lib/src/jvmMain/kotlin/Foo.kt",
            "lib/src/androidMain/kotlin/Foo.kt",
            "lib/src/desktopMain/kotlin/Foo.kt",
            "lib/src/jvmTest/kotlin/FooTest.kt",
        )
        val expected = emptyList<String>()

        val excluded = paths.filter { path -> sut.any { it.matches(Path.of(path)) } }

        assertEquals(expected, excluded)
    }
}
