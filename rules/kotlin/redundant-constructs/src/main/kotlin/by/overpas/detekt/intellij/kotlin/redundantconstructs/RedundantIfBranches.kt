package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.nextLeafs
import org.jetbrains.kotlin.psi.psiUtil.prevLeafs

private const val SIMPLE_BRANCH = "simple"

private const val RETURN_BRANCH = "return"

private const val ASSIGNMENT_BRANCH = "assign "

internal fun KtExpression.redundantIfBranch(): Pair<KtExpression, String>? =
    when (this) {
        is KtReturnExpression -> returnedExpression?.let { it to RETURN_BRANCH + labeledExpression?.text.orEmpty() }
        is KtBlockExpression -> statements.singleOrNull()?.redundantIfBranch()
        is KtBinaryExpression -> assignmentOrSimpleBranch()
        else -> this to SIMPLE_BRANCH
    }

private fun KtBinaryExpression.assignmentOrSimpleBranch(): Pair<KtExpression, String> {
    val left = left
    val right = right
    val isAssignment = operationToken == KtTokens.EQ && left != null && right != null
    return if (isAssignment) right to ASSIGNMENT_BRANCH + left.text else this to SIMPLE_BRANCH
}

internal fun PsiElement.hasRedundantIfComments(previous: PsiElement?): Boolean {
    val fileText = containingFile.text
    val line = StringUtil.offsetToLineNumber(fileText, textRange.startOffset)
    val previousLine = previous?.run { StringUtil.offsetToLineNumber(fileText, textRange.startOffset) }
    val hasPreviousComment = prevLeafs.leadingComments().any {
        StringUtil.offsetToLineNumber(fileText, it.textRange.startOffset) != previousLine
    }
    val hasIfPreviousComment = (parent?.parent as? KtIfExpression)?.run { prevLeafs.leadingComments().any() } == true
    val hasTailComment = nextLeafs.leadingComments().any {
        StringUtil.offsetToLineNumber(fileText, it.textRange.startOffset) == line
    }
    return hasPreviousComment || hasIfPreviousComment || hasTailComment || anyDescendantOfType<PsiComment>()
}

private fun Sequence<PsiElement>.leadingComments(): Sequence<PsiComment> =
    takeWhile { it is PsiWhiteSpace || it is PsiComment }.filterIsInstance<PsiComment>()
