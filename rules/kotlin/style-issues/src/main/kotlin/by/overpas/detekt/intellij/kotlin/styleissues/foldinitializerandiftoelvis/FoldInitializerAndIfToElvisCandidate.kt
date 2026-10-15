package by.overpas.detekt.intellij.kotlin.styleissues.foldinitializerandiftoelvis

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.siblings

internal class FoldInitializerAndIfToElvisCandidate(private val ifExpression: KtIfExpression) {

    private val condition = ifExpression.condition

    private val checkedValue = (condition as? KtBinaryExpression)
        ?.takeIf { it.operationToken == KtTokens.EQEQ }
        ?.let { listOfNotNull(it.left, it.right) }
        ?.takeIf { it.size == 2 }
        ?.singleOrNull { !KtPsiUtil.isNullConstant(it) } as? KtNameReferenceExpression

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
            !isVariableUsedInStatement()

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
