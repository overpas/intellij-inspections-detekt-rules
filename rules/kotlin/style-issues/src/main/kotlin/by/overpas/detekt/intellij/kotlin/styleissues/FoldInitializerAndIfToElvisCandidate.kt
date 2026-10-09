package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.siblings

internal class FoldInitializerAndIfToElvisCandidate(private val ifExpression: KtIfExpression) {

    private val condition = ifExpression.condition

    private val checkedValue: KtNameReferenceExpression? = when (condition) {
        is KtBinaryExpression ->
            listOfNotNull(condition.left, condition.right)
                .takeIf { operands -> condition.operationToken == KtTokens.EQEQ && operands.size == 2 }
                ?.singleOrNull { !KtPsiUtil.isNullConstant(it) }

        is KtIsExpression ->
            condition.leftHandSide.takeIf {
                condition.isNegated && condition.typeReference?.typeElement !is KtNullableType
            }

        else -> null
    } as? KtNameReferenceExpression

    private val variable = ifExpression.siblings(forward = false, withItself = false)
        .filterIsInstance<KtExpression>()
        .firstOrNull() as? KtVariableDeclaration

    private val statement = ifExpression.then.let { then ->
        if (then is KtBlockExpression) then.statements.singleOrNull() else then
    }

    fun isCandidate(): Boolean =
        ifExpression.`else` == null &&
            ifExpression.parent is KtBlockExpression &&
            statement != null &&
            checkedValue != null &&
            variable?.name == checkedValue.getReferencedName() &&
            variable.initializer?.let { FoldInitializerAndIfToElvisInitializer(it).isSimple() } == true

    context(session: KaSession)
    fun isFoldable(): Boolean =
        with(session) { statement?.expressionType?.isNothingType == true } &&
            isTypeCheckPossible() &&
            !isVariableUsedInStatement()

    context(session: KaSession)
    private fun isTypeCheckPossible(): Boolean {
        val checkedType = (condition as? KtIsExpression)?.typeReference
        return checkedType == null ||
            with(session) {
                variable?.run { FoldInitializerAndIfToElvisTypeCheck(checkedType, returnType).isPossible() }
            } == true
    }

    context(session: KaSession)
    private fun isVariableUsedInStatement(): Boolean =
        statement?.collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedName() == variable?.name }
            .orEmpty()
            .any { reference ->
                with(session) {
                    reference.references.filterIsInstance<KtReference>().firstNotNullOfOrNull {
                        it.resolveToSymbol()
                    }?.psi
                } == variable
            }
}
