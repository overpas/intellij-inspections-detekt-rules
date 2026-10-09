package by.overpas.detekt.intellij.kotlin.styleissues

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
private fun KtExpression.isWhenSubjectName(): Boolean =
    context(session) {
        val name = this@isWhenSubjectName as? KtNameReferenceExpression
            ?: (this@isWhenSubjectName as? KtQualifiedExpression)?.selectorExpression as? KtNameReferenceExpression
        val symbol = name?.whenSubjectSymbol()
        val isObject = (symbol as? KaClassSymbol)?.run { classKind.isObject } == true
        val isConst = (symbol as? KaKotlinPropertySymbol)?.isConst == true
        name != null && !isObject && !isConst
    }
