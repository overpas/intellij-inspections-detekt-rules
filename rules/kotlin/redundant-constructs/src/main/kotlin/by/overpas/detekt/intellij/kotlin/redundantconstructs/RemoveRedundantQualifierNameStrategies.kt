@file:OptIn(KaExperimentalApi::class, KaIdeApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaIdeApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.ShortenStrategy
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtUserType

internal fun KaSession.qualifierClassStrategy(symbol: KaClassLikeSymbol): ShortenStrategy {
    val owner = symbol.containingDeclaration
    return if (symbol.isEnumCompanionIn(owner)) {
        ShortenStrategy.DO_NOT_SHORTEN
    } else {
        ShortenStrategy.SHORTEN_IF_ALREADY_IMPORTED
    }
}

internal fun KaSession.qualifierCallableStrategy(symbol: KaCallableSymbol): ShortenStrategy {
    val container = symbol.containingDeclaration
    val containerOwner = container?.containingDeclaration
    val isEnumMember = container.isEnumClass || container.isEnumCompanionIn(containerOwner)
    return if (symbol !is KaEnumEntrySymbol && isEnumMember) {
        ShortenStrategy.DO_NOT_SHORTEN
    } else {
        ShortenStrategy.SHORTEN_IF_ALREADY_IMPORTED
    }
}

internal fun KaSession.keepsResolveUnqualified(element: KtElement): Boolean {
    val reference = when (element) {
        is KtDotQualifiedExpression -> element.selectorExpression as? KtNameReferenceExpression
        is KtUserType -> element.referenceExpression as? KtNameReferenceExpression
        else -> null
    }
    val intendedFqName = reference?.resolveSymbol()?.importableFqName
    return intendedFqName == null ||
        element.containingKtFile.importDirectives.all { it.aliasName == null || it.importedFqName != intendedFqName }
}

private fun KaSymbol?.isEnumCompanionIn(owner: KaSymbol?): Boolean =
    this is KaClassSymbol &&
        classKind == KaClassKind.COMPANION_OBJECT &&
        owner is KaNamedClassSymbol &&
        owner.companionObject == this &&
        owner.isEnumClass

private val KaSymbol?.isEnumClass: Boolean
    get() = (this as? KaClassSymbol)?.classKind == KaClassKind.ENUM_CLASS
