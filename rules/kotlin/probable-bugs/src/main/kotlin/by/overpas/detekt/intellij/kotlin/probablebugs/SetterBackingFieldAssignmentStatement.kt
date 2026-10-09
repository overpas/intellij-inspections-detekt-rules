package by.overpas.detekt.intellij.kotlin.probablebugs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaBackingFieldSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUnaryExpression

internal class SetterBackingFieldAssignmentStatement(private val expression: KtExpression) {

    private val assignmentTokens = setOf(
        KtTokens.EQ,
        KtTokens.PLUSEQ,
        KtTokens.MINUSEQ,
        KtTokens.MULTEQ,
        KtTokens.DIVEQ,
        KtTokens.PERCEQ,
    )

    private val incrementTokens = setOf(KtTokens.PLUSPLUS, KtTokens.MINUSMINUS)

    fun updates(property: KtProperty): Boolean =
        when (expression) {
            is KtBinaryExpression ->
                expression.operationToken in assignmentTokens && expression.left.backingFieldOwner() == property

            is KtUnaryExpression ->
                expression.operationReference.getReferencedNameElementType() in incrementTokens &&
                    expression.baseExpression.backingFieldOwner() == property

            else -> false
        }

    fun forwards(parameter: KtParameter): Boolean =
        expression is KtCallExpression &&
            analyze(expression) {
                val symbol = parameter.symbol
                expression.valueArguments.any { argument ->
                    argument.getArgumentExpression()?.run {
                        references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
                    } == symbol
                }
            }

    private fun KtExpression?.backingFieldOwner(): PsiElement? =
        (this as? KtNameReferenceExpression)
            ?.takeIf { it.text == KtTokens.FIELD_KEYWORD.value }
            ?.let { name ->
                analyze(name) {
                    val symbol = name.references.filterIsInstance<KtReference>().firstNotNullOfOrNull {
                        it.resolveToSymbol()
                    }
                    (symbol as? KaBackingFieldSymbol)?.run { owningProperty.psi }
                }
            }
}
