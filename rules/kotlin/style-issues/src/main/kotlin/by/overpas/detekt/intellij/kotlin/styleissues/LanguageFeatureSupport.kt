package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.projectStructure.KaLibraryModule
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.io.path.isRegularFile

private const val JAR_SEPARATOR = "!/"

private const val KOTLIN_VERSION_CLASS_PATH = "kotlin/KotlinVersion.class"

private const val KOTLIN_VERSION_ATTRIBUTE = "Kotlin-Version"

private val KOTLIN_VERSION_CLASS_ID = ClassId.topLevel(FqName("kotlin.KotlinVersion"))

context(session: KaSession)
private val stdlibLanguageVersion: LanguageVersion?
    get() {
        val symbol = session.findClass(KOTLIN_VERSION_CLASS_ID) ?: return null
        val library = with(session) { symbol.containingModule } as? KaLibraryModule ?: return null
        val jars = StandardFileSystems.jar()
        val manifest = library.binaryRoots.asSequence()
            .filter { it.isRegularFile() }
            .mapNotNull { jars.findFileByPath("$it$JAR_SEPARATOR") }
            .firstOrNull { it.findFileByRelativePath(KOTLIN_VERSION_CLASS_PATH) != null }
            ?.findFileByRelativePath(JarFile.MANIFEST_NAME)
            ?: return null
        return manifest.inputStream.use { Manifest(it) }
            .mainAttributes
            .getValue(KOTLIN_VERSION_ATTRIBUTE)
            ?.let { LanguageVersion.fromVersionString(it) }
    }

context(session: KaSession)
internal val LanguageFeature.isSupportedByUseSiteModule: Boolean?
    get() {
        val settings = (session.useSiteModule as? KaSourceModule)?.languageVersionSettings ?: return null
        val sinceVersion = sinceVersion
        return when {
            settings.getCustomizedLanguageFeatures()[this] == LanguageFeature.State.ENABLED -> true
            !settings.supportsFeature(this) -> false
            else -> sinceVersion == null || stdlibLanguageVersion?.let { it >= sinceVersion } != false
        }
    }
