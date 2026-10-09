package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

private val REPLACE_WITH_OPERATOR_ASSIGNMENT_COMMUTATIVE = setOf(KtTokens.PLUS, KtTokens.MUL)

private val REPLACE_WITH_OPERATOR_ASSIGNMENT_ARITHMETIC =
    setOf(KtTokens.PLUS, KtTokens.MINUS, KtTokens.MUL, KtTokens.DIV, KtTokens.PERC)

private val REPLACE_WITH_OPERATOR_ASSIGNMENT_READ_ONLY =
    setOf(StandardClassIds.List, StandardClassIds.Set, StandardClassIds.Map)

internal class ReplaceWithOperatorAssignmentCandidate(private val assignment: KtBinaryExpression) {

    private val variable = assignment.left

    private val operations =
        generateSequence(assignment.right as? KtBinaryExpression) { it.left as? KtBinaryExpression }.toList()

    private val matchIndex = operations.indexOfFirst { operation ->
        listOf(operation.left, operation.right).any { it != null && it.text == variable?.text }
    }

    private val match = operations.getOrNull(matchIndex)

    private val enclosing = operations.take(matchIndex.coerceAtLeast(0))

    private val isLeftMatch = match?.left?.text == variable?.text

    private val primitiveOperations = if (isLeftMatch) enclosing else enclosing + listOfNotNull(match)

    private val allowedTokens =
        if (isLeftMatch && enclosing.isEmpty()) {
            REPLACE_WITH_OPERATOR_ASSIGNMENT_ARITHMETIC
        } else {
            REPLACE_WITH_OPERATOR_ASSIGNMENT_COMMUTATIVE
        }

    fun isCandidate(): Boolean =
        assignment.operationToken == KtTokens.EQ &&
            variable != null &&
            operations.firstOrNull()?.right != null &&
            match != null &&
            enclosing.all { it.operationToken == match.operationToken } &&
            match.operationToken in allowedTokens

    context(session: KaSession)
    fun isRepeated(): Boolean =
        with(session) {
            val variableType = (variable as? KtNameReferenceExpression)?.expressionType as? KaClassType
            variableType?.classId !in REPLACE_WITH_OPERATOR_ASSIGNMENT_READ_ONLY &&
                primitiveOperations.all { operation ->
                    val function = operation.resolveToCall()?.successfulFunctionCallOrNull()
                    function?.run { symbol.callableId?.classId } in StandardClassIds.primitiveTypes
                }
        }

    fun replacement(): KtBinaryExpression? {
        val operand = (if (isLeftMatch) match?.right else match?.left)?.text.orEmpty()
        val operator = match?.run { operationReference.text }.orEmpty()
        val tail = enclosing.reversed().joinToString(separator = "") { operation ->
            " ${operation.operationReference.text} ${operation.right?.text.orEmpty()}"
        }
        val text = "${variable?.text.orEmpty()} $operator= $operand$tail"
        val fragment = KtPsiFactory(assignment.project).createExpressionCodeFragment(text, assignment)
        return fragment.getContentElement() as? KtBinaryExpression
    }

    context(session: KaSession)
    fun resolves(replacement: KtBinaryExpression): Boolean =
        with(session) {
            replacement.operationReference.references
                .filterIsInstance<KtReference>()
                .firstNotNullOfOrNull { it.resolveToSymbol() } != null
        }
}
