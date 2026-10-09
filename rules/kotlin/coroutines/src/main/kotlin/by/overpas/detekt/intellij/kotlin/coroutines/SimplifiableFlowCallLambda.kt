package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtTypeReference

internal class SimplifiableFlowCallLambda(lambda: KtLambdaExpression?) {

    private val parameterName: String? = lambda?.run {
        if (valueParameters.isEmpty()) "it" else valueParameters.singleOrNull()?.name
    }

    private val statement: KtExpression? = lambda?.bodyExpression?.run { statements.singleOrNull() }

    fun isIdentity(): Boolean =
        statement.isParameterReference()

    fun isNotNullCheck(): Boolean {
        val binary = statement as? KtBinaryExpression
        val left = binary?.left
        val right = binary?.right
        return binary?.operationToken in setOf(KtTokens.EXCLEQ, KtTokens.EXCLEQEQEQ) &&
            (
                (left.isParameterReference() && right.isNullConstant()) ||
                    (right.isParameterReference() && left.isNullConstant())
                )
    }

    fun isInstanceCheckType(): KtTypeReference? =
        (statement as? KtIsExpression)
            ?.takeIf { !it.isNegated && it.leftHandSide.isParameterReference() }
            ?.typeReference

    private fun KtExpression?.isParameterReference(): Boolean =
        parameterName != null && (this as? KtNameReferenceExpression)?.getReferencedName() == parameterName

    private fun KtExpression?.isNullConstant(): Boolean =
        this is KtConstantExpression && node.elementType == KtNodeTypes.NULL
}
