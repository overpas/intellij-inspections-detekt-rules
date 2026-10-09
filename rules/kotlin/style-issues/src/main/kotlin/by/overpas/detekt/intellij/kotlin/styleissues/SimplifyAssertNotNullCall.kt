package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.siblings

private val SIMPLIFY_ASSERT_NOT_NULL_CALLABLE_ID = CallableId(FqName("kotlin"), Name.identifier("assert"))

internal class SimplifyAssertNotNullCall(private val call: KtCallExpression) {

    private val arguments = call.valueArguments.map { it.getArgumentExpression() }

    private val condition = arguments.firstOrNull() as? KtBinaryExpression

    private val checkedValue = listOfNotNull(condition?.left, condition?.right)
        .filterNot { KtPsiUtil.isNullConstant(it) }
        .singleOrNull() as? KtNameReferenceExpression

    private val variable = call.siblings(forward = false, withItself = false)
        .filterIsInstance<KtExpression>()
        .firstOrNull() as? KtVariableDeclaration

    private val message = arguments.getOrNull(1).let { argument ->
        (argument as? KtLambdaExpression)?.bodyExpression?.statements.orEmpty().singleOrNull()
    }

    fun isCandidate(): Boolean =
        call.calleeExpression?.text == "assert" &&
            call.parent is KtBlockExpression &&
            arguments.size in 1..2 &&
            (arguments.size == 1 || message != null) &&
            condition?.operationToken == KtTokens.EXCLEQ &&
            checkedValue != null &&
            variable?.initializer != null &&
            variable.name == checkedValue.getReferencedName()

    context(session: KaSession)
    fun isKotlinAssert(): Boolean =
        with(session) {
            call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } ==
                SIMPLIFY_ASSERT_NOT_NULL_CALLABLE_ID
        }
}
