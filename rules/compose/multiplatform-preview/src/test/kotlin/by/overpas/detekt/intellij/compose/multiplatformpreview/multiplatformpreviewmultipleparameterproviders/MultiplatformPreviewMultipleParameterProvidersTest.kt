package by.overpas.detekt.intellij.compose.multiplatformpreview.multiplatformpreviewmultipleparameterproviders

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplatformPreviewMultipleParameterProvidersTest {

    private val environment = createEnvironment()

    private val sut = MultiplatformPreviewMultipleParameterProviders(Config.empty)

    @Test
    fun `the second preview parameter of a multiplatform preview function is reported`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            @Preview
            fun MyPreview(
                @PreviewParameter a: Int,
                @PreviewParameter b: Int,
            ) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()
        val expected = listOf(7)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `mixed jetpack and multiplatform preview parameters are reported`() {
        val code = """
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(
                @androidx.compose.ui.tooling.preview.PreviewParameter a: Int,
                @org.jetbrains.compose.ui.tooling.preview.PreviewParameter b: Int,
            ) {}
        """.trimIndent()
        val jetpackTooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()
        val multiplatformTooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class PreviewParameter
        """.trimIndent()
        val expected = listOf(6)

        val lines = sut.lintWithContext(environment, code, jetpackTooling, multiplatformTooling)
            .map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `the second preview parameter of a composable function with a multipreview annotation is reported`() {
        val code = """
            import androidx.compose.runtime.Composable
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            @Preview
            annotation class MyAnnotation

            @MyAnnotation
            @Composable
            fun MyPreview(@PreviewParameter a: Int, @PreviewParameter b: Int) {}
        """.trimIndent()
        val runtime = """
            package androidx.compose.runtime

            annotation class Composable
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, runtime, tooling)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function with a multipreview annotation and without composable passes`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            @Preview
            annotation class MyAnnotation

            @MyAnnotation
            fun MyPreview(@PreviewParameter a: Int, @PreviewParameter b: Int) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a preview function with one preview parameter passes`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            @Preview
            fun MyPreview(@PreviewParameter a: Int, b: Int = 1) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function without a preview annotation passes`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            fun NotPreview(@PreviewParameter a: Int, @PreviewParameter b: Int) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class PreviewParameter
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `jetpack preview parameters are left to the active android rule`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewMultipleParameterProviders(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewMultipleParameterProviders"),
        )
        val code = """
            import androidx.compose.ui.tooling.preview.Preview
            import androidx.compose.ui.tooling.preview.PreviewParameter

            @Preview
            fun MyPreview(
                @PreviewParameter a: Int,
                @PreviewParameter b: Int,
            ) {}
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mixed preview parameters are reported when the android rule is active`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewMultipleParameterProviders(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewMultipleParameterProviders"),
        )
        val code = """
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(
                @androidx.compose.ui.tooling.preview.PreviewParameter a: Int,
                @org.jetbrains.compose.ui.tooling.preview.PreviewParameter b: Int,
            ) {}
        """.trimIndent()
        val jetpackTooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()
        val multiplatformTooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class PreviewParameter
        """.trimIndent()
        val expected = listOf(6)

        val lines = sut.lintWithContext(environment, code, jetpackTooling, multiplatformTooling)
            .map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `jetpack preview parameters on a multiplatform preview are reported when the android rule is active`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewMultipleParameterProviders(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewMultipleParameterProviders"),
        )
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import androidx.compose.ui.tooling.preview.PreviewParameter

            @Preview
            fun MyPreview(
                @PreviewParameter a: Int,
                @PreviewParameter b: Int,
            ) {}
        """.trimIndent()
        val jetpackTooling = """
            package androidx.compose.ui.tooling.preview

            annotation class PreviewParameter
        """.trimIndent()
        val multiplatformTooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()
        val expected = listOf(7)

        val lines = sut.lintWithContext(environment, code, jetpackTooling, multiplatformTooling)
            .map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `jetpack preview parameters are reported when the android rule is inactive`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewMultipleParameterProviders" to mapOf("active" to false),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewMultipleParameterProviders" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewMultipleParameterProviders(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewMultipleParameterProviders"),
        )
        val code = """
            import androidx.compose.ui.tooling.preview.Preview
            import androidx.compose.ui.tooling.preview.PreviewParameter

            @Preview
            fun MyPreview(
                @PreviewParameter a: Int,
                @PreviewParameter b: Int,
            ) {}
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview

            annotation class PreviewParameter
        """.trimIndent()
        val expected = listOf(7)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }
}
