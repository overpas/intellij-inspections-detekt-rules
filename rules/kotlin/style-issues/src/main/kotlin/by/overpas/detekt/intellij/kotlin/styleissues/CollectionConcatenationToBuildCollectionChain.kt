package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private const val MIN_OPERATORS = 2

private val OPERATOR_TOKENS = setOf(KtTokens.PLUS, KtTokens.MINUS)

private val RESULT_TYPES = setOf(StandardClassIds.List, StandardClassIds.Set)

internal class CollectionConcatenationToBuildCollectionChain(private val root: KtBinaryExpression) {

    private val operators = generateSequence(root) { it.left as? KtBinaryExpression }.toList()

    val highlight: KtExpression =
        generateSequence<KtExpression>(root) { it.parent as? KtParenthesizedExpression }.last()

    fun isCandidate(): Boolean =
        operators.size >= MIN_OPERATORS &&
            isTopmost() &&
            operators.all { it.operationToken in OPERATOR_TOKENS && it.right != null }

    context(session: KaSession)
    fun isConvertible(): Boolean =
        with(session) { (root.expressionType as? KaClassType)?.classId in RESULT_TYPES } &&
            operators.all { it.isStdlibOperation() }

    private fun isTopmost(): Boolean =
        generateSequence(root.parent) { it.parent }
            .takeWhile { it is KtParenthesizedExpression || it is KtQualifiedExpression || it is KtBinaryExpression }
            .none { it is KtBinaryExpression }

    context(session: KaSession)
    private fun KtBinaryExpression.isStdlibOperation(): Boolean =
        with(session) {
            resolveToCall()?.successfulFunctionCallOrNull()?.symbol?.callableId?.packageName
        } == StandardNames.COLLECTIONS_PACKAGE_FQ_NAME
}
