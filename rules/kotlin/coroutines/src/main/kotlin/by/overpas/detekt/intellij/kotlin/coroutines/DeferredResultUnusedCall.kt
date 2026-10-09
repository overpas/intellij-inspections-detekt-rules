package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
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

internal class DeferredResultUnusedCall(private val expression: KtCallExpression) {

    fun isDiscarded(): Boolean =
        analyze(expression) {
            val call = expression.resolveToCall()?.successfulFunctionCallOrNull()
            val returnType = call?.run { signature.returnType.expandedSymbol?.classId?.asSingleFqName() }
            !expression.isUsedAsExpression &&
                call?.run { symbol.callableId?.asSingleFqName() } !in IGNORED_FQ_NAMES &&
                returnType in DEFERRED_FQ_NAMES
        }
}
