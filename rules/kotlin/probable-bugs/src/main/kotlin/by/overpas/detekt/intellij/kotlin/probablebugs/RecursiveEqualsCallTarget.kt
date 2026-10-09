package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class RecursiveEqualsCallTarget(
    private val expression: KtExpression,
    private val argument: KtNameReferenceExpression,
) {

    @OptIn(KaExperimentalApi::class)
    val isRecursive: Boolean
        get() {
            val function = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
            return analyze(expression) {
                val call = expression.resolveToCall()?.successfulFunctionCallOrNull()
                val calledSymbol = call?.symbol as? KaNamedFunctionSymbol
                val parameter = calledSymbol?.run { valueParameters.singleOrNull() }
                val isEqualsMethod =
                    calledSymbol?.run { name == OperatorNameConventions.EQUALS && isOverride } == true &&
                        parameter?.run { returnType.isAnyType && returnType.isMarkedNullable } == true &&
                        calledSymbol.returnType.run { isBooleanType && !isMarkedNullable }
                val isThisReceiver = when (val receiver = call?.dispatchReceiver) {
                    is KaImplicitReceiverValue -> receiver.symbol == calledSymbol?.containingSymbol
                    is KaExplicitReceiverValue -> receiver.expression is KtThisExpression
                    else -> false
                }
                val argumentSymbol = argument.references
                    .filterIsInstance<KtReference>()
                    .firstNotNullOfOrNull { it.resolveToSymbol() }
                isEqualsMethod && isThisReceiver && calledSymbol == function.symbol && argumentSymbol == parameter
            }
        }
}
