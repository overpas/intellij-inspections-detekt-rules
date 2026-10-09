package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElement
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective

internal class ReplaceWithImportAliasImports(private val reference: KtNameReferenceExpression) {

    private val imports = reference.containingKtFile.importDirectives.filter {
        !it.isAllUnder && it.alias != null && it.importedFqName?.shortName() == reference.getReferencedNameAsName()
    }

    private val isQualified = when (val qualified = reference.getQualifiedElement()) {
        is KtDotQualifiedExpression -> true
        is KtUserType -> qualified.qualifier != null
        else -> false
    }

    fun isCandidate(): Boolean =
        imports.isNotEmpty() && isQualified && reference.getIdentifier() != null && !reference.isInImportDirective()

    fun matchTarget(): Boolean =
        analyze(reference) {
            val fqName = when (
                val symbol = reference.references.filterIsInstance<KtReference>().firstNotNullOfOrNull {
                    it.resolveToSymbol()
                }
            ) {
                is KaConstructorSymbol -> symbol.containingClassId?.asSingleFqName()
                is KaClassLikeSymbol -> symbol.classId?.asSingleFqName()
                is KaCallableSymbol -> symbol.callableId?.asSingleFqName()
                else -> null
            }
            fqName != null && imports.any { it.importedFqName == fqName }
        }
}
