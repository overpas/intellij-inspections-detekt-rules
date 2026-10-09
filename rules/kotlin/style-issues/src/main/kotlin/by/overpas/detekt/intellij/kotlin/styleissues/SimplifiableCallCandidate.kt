package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReferenceExpression

private val SIMPLIFIABLE_CALL_NAMES = setOf("flatMap", "filter", "mapNotNull")

private val SIMPLIFIABLE_CALL_NOT_EQUAL_TOKENS = setOf(KtTokens.EXCLEQ, KtTokens.EXCLEQEQEQ)

internal class SimplifiableCallCandidate(private val call: KtCallExpression) {

    private val name = call.calleeExpression?.text

    private val lambda = call.valueArguments.singleOrNull()?.let { argument ->
        (argument as? KtLambdaArgument)?.getLambdaExpression()
            ?: argument.getArgumentExpression() as? KtLambdaExpression
    }

    private val parameterName = lambda?.let { literal ->
        if (literal.valueParameters.isEmpty()) "it" else literal.valueParameters.singleOrNull()?.name
    }

    private val statement = lambda?.run { bodyExpression?.statements.orEmpty().singleOrNull() }

    private val isIdentity = (statement as? KtNameReferenceExpression)?.getReferencedName() == parameterName

    private val isNotNullCheck = (statement as? KtBinaryExpression)?.run {
        val operands = listOfNotNull(left, right)
        operationToken in SIMPLIFIABLE_CALL_NOT_EQUAL_TOKENS &&
            operands.any { KtPsiUtil.isNullConstant(it) } &&
            operands.any { (it as? KtNameReferenceExpression)?.getReferencedName() == parameterName }
    } == true

    private val instanceCheck = (statement as? KtIsExpression)?.takeIf { check ->
        !check.isNegated && (check.leftHandSide as? KtNameReferenceExpression)?.getReferencedName() == parameterName
    }?.typeReference

    private val castType = (statement as? KtBinaryExpressionWithTypeRHS)?.takeIf { cast ->
        (cast.left as? KtReferenceExpression)?.text in setOf("it", parameterName)
    }?.right

    fun isCandidate(): Boolean =
        name in SIMPLIFIABLE_CALL_NAMES && parameterName != null && statement != null

    context(session: KaSession)
    fun replacement(): String? =
        with(session) {
            val resolved = call.resolveToCall()?.successfulFunctionCallOrNull()
            val isMapReceiver = resolved?.extensionReceiver?.run { type.isSubtypeOf(StandardClassIds.Map) } == true
            val callableId = CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier(name.orEmpty()))
            when {
                resolved == null || resolved.symbol.callableId != callableId -> null

                name == "flatMap" -> "flatten()".takeIf { isIdentity && resolved.simplifiableCallFlattens() }

                isMapReceiver -> null

                name == "mapNotNull" -> castType?.let { "filterIsInstance<${it.text}>()" }

                isNotNullCheck -> "filterNotNull()"

                else ->
                    instanceCheck
                        ?.takeIf { resolved.simplifiableCallAccepts(it) }
                        ?.let { "filterIsInstance<${it.text}>()" }
            }
        }
}
