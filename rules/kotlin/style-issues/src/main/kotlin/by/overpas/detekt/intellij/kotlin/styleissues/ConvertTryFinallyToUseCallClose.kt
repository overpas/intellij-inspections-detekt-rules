package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtTryExpression

private val CONVERT_TRY_FINALLY_TO_USE_CALL_CLOSEABLES = setOf(
    ClassId.topLevel(FqName("java.io.Closeable")),
    ClassId.topLevel(FqName("java.lang.AutoCloseable")),
)

internal class ConvertTryFinallyToUseCallClose(expression: KtTryExpression) {

    private val statement = expression.finallyBlock?.run { finalExpression.statements.singleOrNull() }

    private val call = (statement as? KtQualifiedExpression)?.selectorExpression as? KtCallExpression
        ?: statement as? KtCallExpression

    private val hasSimpleReceiver: Boolean
        get() = (statement as? KtQualifiedExpression)?.receiverExpression
            .let { it == null || it is KtThisExpression || it is KtNameReferenceExpression }

    val isCloseableClose: Boolean
        get() {
            val close = call ?: return false
            return close.calleeExpression?.text == "close" &&
                close.valueArguments.isEmpty() &&
                hasSimpleReceiver &&
                analyze(close) {
                    val symbol = close.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
                    symbol?.allOverriddenSymbols.orEmpty().any {
                        (it.containingSymbol as? KaClassSymbol)?.classId in CONVERT_TRY_FINALLY_TO_USE_CALL_CLOSEABLES
                    }
                }
        }
}
