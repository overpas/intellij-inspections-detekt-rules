package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis

internal fun KtExpression?.liftableReturn(): KtReturnExpression? =
    (this?.lastBlockStatementOrThis() as? KtReturnExpression)?.takeIf { returnExpression ->
        val returned = returnExpression.returnedExpression
        returned != null && returned !is KtLambdaExpression && returnExpression.getTargetLabel() == null
    }

internal fun KtExpression?.liftableAssignment(): KtBinaryExpression? =
    (this?.lastBlockStatementOrThis() as? KtBinaryExpression)?.takeIf { assignment ->
        val left = assignment.left as? KtNameReferenceExpression
        val block = assignment.parent as? KtBlockExpression
        val isDeclaredInBlock = left != null &&
            block != null &&
            KtPsiUtil.checkVariableDeclarationInBlock(block, left.text)
        val isAssignment = assignment.operationToken in KtTokens.ALL_ASSIGNMENTS && assignment.right != null
        isAssignment && left != null && !isDeclaredInBlock
    }

internal fun KtExpression.isLiftSingleStatement(): Boolean =
    (parent as? KtBlockExpression)?.statements.orEmpty().size <= 1
