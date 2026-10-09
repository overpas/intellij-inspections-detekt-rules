package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulConstructorCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private val replaceAssociateFunctionPairClassId = ClassId(FqName("kotlin"), Name.identifier("Pair"))

internal class ReplaceAssociateFunctionLambda(
    session: KaSession,
    literal: KtFunctionLiteral,
) {

    private val statements = literal.bodyExpression?.statements.orEmpty()

    private val pairParts = when (val last = statements.lastOrNull()) {
        is KtBinaryExpression -> listOf(last.left, last.right).takeIf { last.operationReference.text == "to" }

        is KtCallExpression -> with(session) {
            val classId = last.resolveToCall()
                ?.successfulConstructorCallOrNull()
                ?.run { symbol.containingClassId }
            last.valueArguments
                .map { it.getArgumentExpression() }
                .takeIf { last.calleeExpression?.text == "Pair" && classId == replaceAssociateFunctionPairClassId }
        }

        else -> null
    }

    private val parameterReferences = with(session) {
        val parameter = literal.symbol.valueParameters.singleOrNull()
        pairParts
            ?.takeIf { it.size == 2 && it.all { part -> part != null } && parameter != null }
            ?.map { part ->
                val reference = part as? KtNameReferenceExpression
                reference?.resolveToCall()?.successfulVariableAccessCall()?.symbol == parameter
            }
    }

    val replacement: String? = when {
        parameterReferences == null -> null
        parameterReferences.first() -> "associateWith"
        parameterReferences.last() -> "associateBy"
        statements.size == 1 -> "associateBy"
        else -> null
    }
}
