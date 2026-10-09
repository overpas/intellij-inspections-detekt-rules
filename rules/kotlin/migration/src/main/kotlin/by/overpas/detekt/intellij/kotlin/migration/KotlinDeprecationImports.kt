package by.overpas.detekt.intellij.kotlin.migration

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaDeclarationSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementSelector

private const val DEPRECATION_SUPPRESSION = "DEPRECATION"

context(session: KaSession)
internal fun KtFile.deprecatedImports(): List<KtImportDirective> {
    val isDeprecationSuppressed = annotationEntries.any { entry ->
        val type = with(session) { entry.typeReference?.type } as? KaClassType
        type?.classId == StandardClassIds.Annotations.Suppress &&
            entry.valueArguments.any { argument ->
                val text = argument.getArgumentExpression() as? KtStringTemplateExpression
                text?.entries?.singleOrNull()?.text.equals(DEPRECATION_SUPPRESSION, ignoreCase = true)
            }
    }
    return if (isDeprecationSuppressed) emptyList() else importDirectives.filter { it.isDeprecatedImport() }
}

context(session: KaSession)
private fun KtImportDirective.isDeprecatedImport(): Boolean {
    val symbols = importedReference
        ?.getQualifiedElementSelector()
        ?.references
        .orEmpty()
        .asSequence()
        .filterIsInstance<KtReference>()
        .flatMap { with(session) { it.resolveToSymbols() } }
        .filterIsInstance<KaDeclarationSymbol>()
        .toList()
    return !isAllUnder && symbols.isNotEmpty() && symbols.all { it.hasReplaceWith() }
}
