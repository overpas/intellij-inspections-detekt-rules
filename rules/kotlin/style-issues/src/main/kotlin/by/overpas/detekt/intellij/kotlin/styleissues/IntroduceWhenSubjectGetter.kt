package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression

context(session: KaSession)
internal fun KtExpression.mayInvokeWhenSubjectGetter(): Boolean {
    val expression = KtPsiUtil.safeDeparenthesize(this)
    val symbol = (expression as? KtNameReferenceExpression)?.whenSubjectSymbol()
    return when {
        expression is KtQualifiedExpression ->
            expression.receiverExpression.mayInvokeWhenSubjectGetter() ||
                expression.selectorExpression?.mayInvokeWhenSubjectGetter() == true

        symbol is KaSyntheticJavaPropertySymbol -> true

        symbol is KaPropertySymbol -> symbol.getter?.isNotDefault == true

        else -> false
    }
}
