package by.overpas.detekt.intellij.kotlin.styleissues.introducewhensubject

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaKotlinPropertySymbol
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal fun KtExpression.hasWhenSubjectShape(): Boolean =
    this is KtNameReferenceExpression ||
        (this as? KtQualifiedExpression)?.selectorExpression is KtNameReferenceExpression ||
        this is KtThisExpression

context(session: KaSession)
internal fun KtBinaryExpression.whenSubjectEqualityOperand(): KtExpression? =
    left?.takeIf { it.isWhenSubjectName() } ?: right?.takeIf { it.isWhenSubjectName() }

context(session: KaSession)
private fun KtExpression.isWhenSubjectName(): Boolean {
    val name = this as? KtNameReferenceExpression
        ?: (this as? KtQualifiedExpression)?.selectorExpression as? KtNameReferenceExpression
    val symbol = name?.whenSubjectSymbol()
    val isObject = (symbol as? KaClassSymbol)?.run { classKind.isObject } == true
    val isConst = (symbol as? KaKotlinPropertySymbol)?.isConst == true
    return name != null && !isObject && !isConst
}
