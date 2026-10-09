package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtThisExpression

private val RANGE_OPERATIONS = TokenSet.create(KtTokens.RANGE, KtTokens.RANGE_UNTIL)

private val ARITHMETIC_OPERATIONS = TokenSet.orSet(
    RANGE_OPERATIONS,
    TokenSet.create(KtTokens.PLUS, KtTokens.MINUS, KtTokens.MUL, KtTokens.DIV, KtTokens.PERC),
)

private val SIGN_OPERATIONS = TokenSet.create(KtTokens.PLUS, KtTokens.MINUS)

private val SIMPLE_CONSTANTS = TokenSet.create(
    KtNodeTypes.INTEGER_CONSTANT,
    KtNodeTypes.BOOLEAN_CONSTANT,
    KtNodeTypes.NULL,
)

private val FLOATING_POINT_TYPES = setOf("Double", "Float")

private val FLOATING_POINT_CONSTANTS = setOf("NaN", "POSITIVE_INFINITY", "NEGATIVE_INFINITY")

private val KtStringTemplateExpression.isLiteral: Boolean
    get() = entries.all { it is KtLiteralStringTemplateEntry || it is KtEscapeStringTemplateEntry }

private val KtExpression.isTrueConstant: Boolean
    get() = KtPsiUtil.isTrueConstant(this)

private val KtExpression.isFalseConstant: Boolean
    get() = KtPsiUtil.isFalseConstant(this)

internal val KtExpression.isBooleanConstant: Boolean
    get() = isTrueConstant || isFalseConstant

internal fun Pair<KtExpression, KtExpression>.areOppositeBooleanConstants(): Boolean =
    (first.isTrueConstant && second.isFalseConstant) || (first.isFalseConstant && second.isTrueConstant)

internal fun Pair<KtExpression, KtExpression>.pairConstantWithReusable(hasCommentedBranch: Boolean): Boolean =
    (first.isBooleanConstant && second.canReuseAsBooleanBranch(hasCommentedBranch)) ||
        (first.canReuseAsBooleanBranch(hasCommentedBranch) && second.isBooleanConstant)

internal fun KtBinaryExpression.hasSimpleOperandsWithConstant(): Boolean {
    val left = left
    val right = right
    return left != null &&
        right != null &&
        left.isSimpleOperand() &&
        right.isSimpleOperand() &&
        (left.isSimpleConstantOperand() || right.isSimpleConstantOperand())
}

internal fun KtExpression.isSimpleOperand(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        is KtConstantExpression, is KtNameReferenceExpression, is KtThisExpression -> true

        is KtStringTemplateExpression -> expression.isLiteral

        is KtDotQualifiedExpression, is KtSafeQualifiedExpression -> (expression as KtQualifiedExpression).run {
            receiverExpression.isSimpleOperand() && selectorExpression?.isSimpleOperand() == true
        }

        is KtPrefixExpression ->
            expression.operationToken in SIGN_OPERATIONS && expression.baseExpression?.isSimpleOperand() == true

        is KtBinaryExpression ->
            expression.operationToken in ARITHMETIC_OPERATIONS &&
                expression.left?.isSimpleOperand() == true &&
                expression.right?.isSimpleOperand() == true

        else -> false
    }

internal fun KtExpression.isSimpleConstantOperand(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        is KtConstantExpression -> expression.node.elementType in SIMPLE_CONSTANTS

        is KtStringTemplateExpression -> expression.isLiteral

        is KtDotQualifiedExpression ->
            (expression.receiverExpression as? KtNameReferenceExpression)?.getReferencedName() in
                FLOATING_POINT_TYPES &&
                (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() in
                FLOATING_POINT_CONSTANTS

        is KtPrefixExpression ->
            expression.operationToken in SIGN_OPERATIONS && expression.baseExpression?.isSimpleConstantOperand() == true

        is KtBinaryExpression ->
            expression.operationToken in RANGE_OPERATIONS &&
                expression.left?.isSimpleConstantOperand() == true &&
                expression.right?.isSimpleConstantOperand() == true

        else -> false
    }

internal fun KtExpression.isSimpleBranchLiteral(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        is KtConstantExpression -> expression.node.elementType in SIMPLE_CONSTANTS

        is KtPrefixExpression ->
            expression.operationToken in SIGN_OPERATIONS && expression.baseExpression?.isSimpleBranchLiteral() == true

        else -> false
    }
