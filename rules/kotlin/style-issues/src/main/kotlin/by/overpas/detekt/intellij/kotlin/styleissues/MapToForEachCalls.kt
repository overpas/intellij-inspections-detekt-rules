package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

private val MAP_TO_FOR_EACH_FUNCTIONS = setOf(
    FqName("kotlin.collections.map"),
    FqName("kotlin.collections.mapIndexed"),
    FqName("kotlin.collections.mapNotNull"),
    FqName("kotlin.collections.onEach"),
    FqName("kotlin.collections.onEachIndexed"),
)

internal fun KtCallExpression.isMapToForEachCandidate(): Boolean {
    val calleeText = calleeExpression?.text
    val imports = containingKtFile.importDirectives
    val hasMapName = MAP_TO_FOR_EACH_FUNCTIONS.any { fqName ->
        calleeText == fqName.shortName().asString() ||
            imports.any { it.importedFqName == fqName && it.aliasName == calleeText }
    }
    return hasMapName && valueArguments.size == 1 && KtPsiUtil.isStatement(getQualifiedExpressionForSelectorOrThis())
}

context(session: KaSession)
internal fun KtCallExpression.isMapToForEachReplaceable(): Boolean {
    val fqName = with(session) { resolveToCall()?.successfulFunctionCallOrNull()?.symbol?.callableId?.asSingleFqName() }
    val isUsed = with(session) { getQualifiedExpressionForSelectorOrThis().isUsedAsExpression }
    return fqName in MAP_TO_FOR_EACH_FUNCTIONS && !isUsed && hasOnlyUnitMapReturns()
}

context(session: KaSession)
private fun KtCallExpression.hasOnlyUnitMapReturns(): Boolean {
    val function = when (val argument = valueArguments.single().getArgumentExpression()) {
        is KtLambdaExpression -> argument.functionLiteral
        is KtNamedFunction -> argument
        else -> null
    }
    val nonUnitReturns = function?.collectDescendantsOfType<KtReturnExpression> { returnExpression ->
        val returned = returnExpression.returnedExpression
        val target = with(session) {
            returnExpression.getTargetLabel()
                ?.run { references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() } }
                ?.psi
        }
        target == function && returned != null && returned.text != "Unit"
    }
    return nonUnitReturns.isNullOrEmpty()
}
