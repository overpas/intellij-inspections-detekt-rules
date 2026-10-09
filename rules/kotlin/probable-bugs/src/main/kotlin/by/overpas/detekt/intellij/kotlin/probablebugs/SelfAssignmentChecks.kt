package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtVariableDeclaration

internal fun KtExpression.selfAssignmentReference(): KtNameReferenceExpression? =
    when (this) {
        is KtNameReferenceExpression -> this

        is KtDotQualifiedExpression ->
            (selectorExpression as? KtNameReferenceExpression)?.takeIf {
                receiverExpression is KtThisExpression || receiverExpression is KtNameReferenceExpression
            }

        else -> null
    }

context(session: KaSession)
internal fun KtBinaryExpression.selfAssignmentVariableName(): String? {
    val leftSide = left
    val rightSide = right
    val symbol = leftSide?.selfAssignmentVariable()
    val declaration = symbol?.psi as? KtVariableDeclaration
    val isSelfAssignment = symbol != null &&
        symbol == rightSide?.selfAssignmentVariable() &&
        leftSide.selfAssignmentReceiver() == rightSide.selfAssignmentReceiver()
    return declaration?.takeIf { isSelfAssignment && it.isPlainVar() }?.name
}

context(session: KaSession)
private fun KtVariableDeclaration.isPlainVar(): Boolean {
    val property = this as? KtProperty
    val modality = with(session) { (property?.symbol as? KaPropertySymbol)?.modality }
    val isOverridable = !hasModifier(KtTokens.PRIVATE_KEYWORD) &&
        modality != null &&
        modality != KaSymbolModality.FINAL
    return isVar && !isOverridable && property?.accessors.orEmpty().none { it.hasBody() }
}
