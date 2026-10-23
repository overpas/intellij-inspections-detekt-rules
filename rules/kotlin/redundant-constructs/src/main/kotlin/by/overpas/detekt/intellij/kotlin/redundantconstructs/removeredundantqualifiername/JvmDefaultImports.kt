package by.overpas.detekt.intellij.kotlin.redundantconstructs.removeredundantqualifiername

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile

private val JVM_DEFAULT_IMPORTS = setOf(FqName("java.lang"), FqName("kotlin.jvm"))

private val COMMON_SOURCE_SET = Regex("""(^|/)common\w*(Main|Test)/""")

internal fun KtFile.reliesOnJvmDefaultImport(fqName: FqName): Boolean {
    val packageName = fqName.parent()
    return packageName in JVM_DEFAULT_IMPORTS &&
        COMMON_SOURCE_SET.containsMatchIn(virtualFilePath) &&
        importDirectives.none { it.importedFqName == if (it.isAllUnder) packageName else fqName }
}
