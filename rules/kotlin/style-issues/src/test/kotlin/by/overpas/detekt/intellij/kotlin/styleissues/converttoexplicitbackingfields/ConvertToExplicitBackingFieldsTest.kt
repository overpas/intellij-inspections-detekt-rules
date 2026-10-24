package by.overpas.detekt.intellij.kotlin.styleissues.converttoexplicitbackingfields

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import dev.detekt.test.utils.createEnvironment
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.jar.JarFile
import kotlin.io.path.copyTo
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertToExplicitBackingFieldsTest {

    private val environment = createEnvironment()

    private val sut = ConvertToExplicitBackingFields(Config.empty)

    @Test
    fun `a top-level property that returns a private mutable set is reported`() {
        val code = """
            private val _items = mutableSetOf<Int>()
            val items: Set<Int>
                get() = _items
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a class property that returns a private subtype is reported`() {
        val code = """
            interface A
            class B : A

            class C {
                private val _items = B()
                val items: A get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter that returns the backing property through this is reported`() {
        val code = """
            class UserViewModel {
                private val _names: MutableSet<String>

                val x: Set<String>
                    get() = this._names

                init {
                    this._names = mutableSetOf("John")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter with a block body that returns the backing property is reported`() {
        val code = """
            class Foo {
                private val _x = mutableListOf<Int>()
                val x: List<Int>
                    get() {
                        return _x
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a final override in an abstract class is reported`() {
        val code = """
            interface Base {
                val names: List<String>
            }

            abstract class AbstractBase : Base {
                private val _names = mutableListOf<String>()
                final override val names: List<String> get() = _names
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of a sealed class that is not open is reported`() {
        val code = """
            sealed class Test {
                private val _items = mutableSetOf<Int>()

                val items: Set<Int>
                    get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with an out-projected generic type is reported`() {
        val code = """
            class MyClass<out T>

            private val _y = MyClass<String>()
            val y: MyClass<Any>
                get() = _y
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a backing property of the same type passes`() {
        val code = """
            class MyClass

            private val _x = MyClass()
            val x: MyClass
                get() = _x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backing property that is a var passes`() {
        val code = """
            class Foo {
                private var _x = mutableListOf<Int>()
                val x: List<Int> get() = _x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backing property with a getter passes`() {
        val code = """
            class A {
                private val _storage: String
                    get() = "dynamic"

                val data: CharSequence
                    get() = _storage
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegated backing property passes`() {
        val code = """
            class Foo {
                private val _x by lazy { mutableListOf(1, 2, 3) }
                val x: List<Int> get() = _x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a backing property that is not private passes`() {
        val code = """
            val _x = mutableListOf<String>()
            val x: List<String> get() = _x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with more logic passes`() {
        val code = """
            private val _x = mutableListOf<String>()
            val x: List<String> get() {
                return _x + listOf("a", "b")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension property passes`() {
        val code = """
            class Holder

            private val _items = mutableSetOf<Int>()

            val Holder.items: Set<Int>
                get() = _items
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open property passes`() {
        val code = """
            open class Test {
                private val _items = mutableSetOf<Int>()

                open val items: Set<Int>
                    get() = _items
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an override in an abstract class passes`() {
        val code = """
            interface Base {
                val names: List<String>
            }

            abstract class AbstractBase : Base {
                private val _names = mutableListOf<String>()
                override val names: List<String> get() = _names
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var property passes`() {
        val code = """
            private val _x = mutableListOf<String>()
            var x: List<String>
                get() = _x
                set(value) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property passes when the language version does not support explicit backing fields`() {
        val code = """
            private val _items = mutableSetOf<Int>()
            val items: Set<Int>
                get() = _items
        """.trimIndent()
        val languageVersionSettings = LanguageVersionSettingsImpl(
            languageVersion = LanguageVersion.KOTLIN_2_3,
            apiVersion = ApiVersion.KOTLIN_2_3,
        )

        val findings = sut.lintWithContext(environment, code, languageVersionSettings = languageVersionSettings)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property is reported when the language version supports explicit backing fields`() {
        val code = """
            private val _items = mutableSetOf<Int>()
            val items: Set<Int>
                get() = _items
        """.trimIndent()
        val languageVersionSettings = LanguageVersionSettingsImpl(
            languageVersion = LanguageVersion.KOTLIN_2_4,
            apiVersion = ApiVersion.KOTLIN_2_4,
        )

        val findings = sut.lintWithContext(environment, code, languageVersionSettings = languageVersionSettings)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property passes when the stdlib does not support explicit backing fields`() {
        val code = """
            private val _items = mutableSetOf<Int>()
            val items: Set<Int>
                get() = _items
        """.trimIndent()
        val stdlib = createTempFile(suffix = ".jar").also { it.toFile().deleteOnExit() }
        val stdlibLocation = KotlinVersion::class.java.protectionDomain.codeSource.location
        Path.of(stdlibLocation.toURI()).copyTo(stdlib, overwrite = true)
        FileSystems.newFileSystem(stdlib).use { jar ->
            jar.getPath(JarFile.MANIFEST_NAME).writeText("Manifest-Version: 1.0\nKotlin-Version: 2.3\n")
        }
        val environment = KotlinEnvironmentContainer(emptyList(), listOf(stdlib))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property is reported when explicit backing fields are enabled explicitly with an older stdlib`() {
        val code = """
            private val _items = mutableSetOf<Int>()
            val items: Set<Int>
                get() = _items
        """.trimIndent()
        val stdlib = createTempFile(suffix = ".jar").also { it.toFile().deleteOnExit() }
        val stdlibLocation = KotlinVersion::class.java.protectionDomain.codeSource.location
        Path.of(stdlibLocation.toURI()).copyTo(stdlib, overwrite = true)
        FileSystems.newFileSystem(stdlib).use { jar ->
            jar.getPath(JarFile.MANIFEST_NAME).writeText("Manifest-Version: 1.0\nKotlin-Version: 2.3\n")
        }
        val environment = KotlinEnvironmentContainer(emptyList(), listOf(stdlib))
        val languageVersionSettings = LanguageVersionSettingsImpl(
            languageVersion = LanguageVersion.KOTLIN_2_3,
            apiVersion = ApiVersion.KOTLIN_2_3,
            specificFeatures = mapOf(LanguageFeature.ExplicitBackingFields to LanguageFeature.State.ENABLED),
        )

        val findings = sut.lintWithContext(environment, code, languageVersionSettings = languageVersionSettings)

        assertEquals(1, findings.size)
    }
}
