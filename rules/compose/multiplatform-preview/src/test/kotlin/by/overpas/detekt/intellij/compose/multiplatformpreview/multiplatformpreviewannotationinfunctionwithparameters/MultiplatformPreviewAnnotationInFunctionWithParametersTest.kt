package by.overpas.detekt.intellij.compose.multiplatformpreview.multiplatformpreviewannotationinfunctionwithparameters

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplatformPreviewAnnotationInFunctionWithParametersTest {

    private val environment = createEnvironment()

    private val sut = MultiplatformPreviewAnnotationInFunctionWithParameters(Config.empty)

    @Test
    fun `a multiplatform preview function with a parameter without a default value is reported`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(a: Int, b: String = "hello") {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()
        val expected = listOf(3)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a jetpack preview function with a parameter without a default value is reported`() {
        val code = """
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()
        val expected = listOf(3)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `each preview annotation of a function is reported`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview

            @Preview
            @Preview
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            @Repeatable
            annotation class Preview
        """.trimIndent()
        val expected = listOf(3, 4)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a composable function with a multipreview annotation and a parameter without a default value is reported`() {
        val code = """
            import androidx.compose.runtime.Composable
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            @Preview
            annotation class MyAnnotation

            @MyAnnotation
            @Composable
            fun MyPreview(@PreviewParameter a: Int, b: String) {}
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
        val expected = listOf(8)

        val lines = sut.lintWithContext(environment, code, runtime, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a composable function with a nested multipreview annotation is reported`() {
        val code = """
            import androidx.compose.runtime.Composable
            import org.jetbrains.compose.ui.tooling.preview.Preview

            @Preview
            annotation class Inner

            @Inner
            annotation class Outer

            @Outer
            @Composable
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val runtime = """
            package androidx.compose.runtime

            annotation class Composable
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, runtime, tooling)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function with a multipreview annotation and without composable passes`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview

            @Preview
            annotation class MyAnnotation

            @MyAnnotation
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a composable function with an annotation without previews passes`() {
        val code = """
            import androidx.compose.runtime.Composable

            annotation class MyEmptyAnnotation

            @MyEmptyAnnotation
            @Composable
            fun NotMultiPreview(a: Int) {}
        """.trimIndent()
        val runtime = """
            package androidx.compose.runtime

            annotation class Composable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, runtime)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a compose annotation outside a preview package is not a multipreview`() {
        val code = """
            import androidx.compose.runtime.Composable
            import org.jetbrains.compose.ui.Tooling

            @Tooling
            @Composable
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val runtime = """
            package androidx.compose.runtime

            annotation class Composable
        """.trimIndent()
        val ui = """
            package org.jetbrains.compose.ui

            import org.jetbrains.compose.ui.tooling.preview.Preview

            @Preview
            annotation class Tooling
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, runtime, ui, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a preview function with default values and a preview parameter passes`() {
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview
            import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

            @Preview
            fun MyPreview(@PreviewParameter a: Int, b: String = "hello") {}
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
    fun `a jetpack preview function is left to the active android rule`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewAnnotationInFunctionWithParameters(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewAnnotationInFunctionWithParameters"),
        )
        val code = """
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a jetpack multipreview is left to the active android rule`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewAnnotationInFunctionWithParameters(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewAnnotationInFunctionWithParameters"),
        )
        val code = """
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            annotation class MyAnnotation

            @MyAnnotation
            @Composable
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val runtime = """
            package androidx.compose.runtime

            annotation class Composable
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, runtime, tooling)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a multiplatform preview function is reported when the android rule is active`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewAnnotationInFunctionWithParameters(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewAnnotationInFunctionWithParameters"),
        )
        val code = """
            import org.jetbrains.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package org.jetbrains.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()
        val expected = listOf(3)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a jetpack preview function is reported when the android rule is inactive`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "PreviewAnnotationInFunctionWithParameters" to mapOf("active" to false),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewAnnotationInFunctionWithParameters(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewAnnotationInFunctionWithParameters"),
        )
        val code = """
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()
        val expected = listOf(3)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a jetpack preview function is reported when the android rule set is inactive`() {
        val config = TestConfig(
            "intellij-android-compose-preview" to mapOf(
                "active" to false,
                "PreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
            "intellij-compose-multiplatform-preview" to mapOf(
                "MultiplatformPreviewAnnotationInFunctionWithParameters" to mapOf("active" to true),
            ),
        )
        val sut = MultiplatformPreviewAnnotationInFunctionWithParameters(
            config.subConfig("intellij-compose-multiplatform-preview")
                .subConfig("MultiplatformPreviewAnnotationInFunctionWithParameters"),
        )
        val code = """
            import androidx.compose.ui.tooling.preview.Preview

            @Preview
            fun MyPreview(a: Int) {}
        """.trimIndent()
        val tooling = """
            package androidx.compose.ui.tooling.preview

            annotation class Preview
        """.trimIndent()
        val expected = listOf(3)

        val lines = sut.lintWithContext(environment, code, tooling).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }
}
