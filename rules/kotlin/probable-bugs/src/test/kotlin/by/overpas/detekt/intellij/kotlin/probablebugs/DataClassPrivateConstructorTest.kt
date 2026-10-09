package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class DataClassPrivateConstructorTest {

    private val environment = createEnvironment()

    private val sut = DataClassPrivateConstructor(Config.empty)

    @Test
    fun `a private constructor of a data class is reported`() {
        val code = """
            data class Foo private constructor(val foo: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a private constructor of a nested data class is reported`() {
        val code = """
            class Outer {
                data class Foo private constructor(val foo: String)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a private constructor of a regular class passes`() {
        val code = """
            class Foo private constructor(val foo: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a public constructor of a data class passes`() {
        val code = """
            data class Foo(val foo: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an internal constructor of a data class passes`() {
        val code = """
            data class Foo internal constructor(val foo: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a data class annotated with ConsistentCopyVisibility passes`() {
        val code = """
            @ConsistentCopyVisibility
            data class Foo private constructor(val foo: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a data class annotated with ExposedCopyVisibility passes`() {
        val code = """
            @ExposedCopyVisibility
            data class Foo private constructor(val foo: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a private constructor of a data class passes when copy respects the constructor visibility`() {
        val code = """
            data class Foo private constructor(val foo: String)
        """.trimIndent()
        val languageVersionSettings = LanguageVersionSettingsImpl(
            languageVersion = LanguageVersion.LATEST_STABLE,
            apiVersion = ApiVersion.LATEST_STABLE,
            analysisFlags = emptyMap(),
            specificFeatures = mapOf(
                LanguageFeature.DataClassCopyRespectsConstructorVisibility to LanguageFeature.State.ENABLED,
            ),
        )

        val findings = sut.lintWithContext(environment, code, languageVersionSettings = languageVersionSettings)

        assertEquals(0, findings.size)
    }
}
