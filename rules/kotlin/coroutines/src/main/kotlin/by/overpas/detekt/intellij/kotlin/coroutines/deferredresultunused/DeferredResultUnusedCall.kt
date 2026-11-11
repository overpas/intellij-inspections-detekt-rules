package by.overpas.detekt.intellij.kotlin.coroutines.deferredresultunused

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression

private val DEFERRED_FQ_NAMES = setOf(
    FqName("kotlinx.coroutines.Deferred"),
    FqName("kotlinx.coroutines.experimental.Deferred"),
)

private val IGNORED_FQ_NAMES = setOf(
    FqName("kotlin.test.assertNotNull"),
    FqName("kotlin.requireNotNull"),
    FqName("kotlin.checkNotNull"),
)

private val STANDARD_FQ_NAMES = setOf(
    FqName("kotlinx.coroutines.async"),
    FqName("kotlinx.coroutines.experimental.async"),
)

internal class DeferredResultUnusedCall(
    private val expression: KtCallExpression,
    private val isStandardOnly: Boolean,
) {

    fun isDiscarded(): Boolean =
        analyze(expression) {
            val call = expression.resolveToCall()?.successfulFunctionCallOrNull()
            val returnType = call?.takeUnless { it.symbol.returnType is KaTypeParameterType }
                ?.run { signature.returnType.expandedSymbol?.classId?.asSingleFqName() }
            val fqName = call?.run { symbol.callableId?.asSingleFqName() }
            val isDeferredCall = if (isStandardOnly) fqName in STANDARD_FQ_NAMES else returnType in DEFERRED_FQ_NAMES
            !expression.isUsedAsExpression && fqName !in IGNORED_FQ_NAMES && isDeferredCall
        }
}
