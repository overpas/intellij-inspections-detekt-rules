package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal class SafeCastWithReturnElvis(private val elvis: KtBinaryExpression) {

    private val cast = elvis.left?.let { KtPsiUtil.safeDeparenthesize(it) } as? KtBinaryExpressionWithTypeRHS

    private val lambda = cast?.getStrictParentOfType<KtLambdaExpression>()

    fun isCandidate(): Boolean =
        elvis.operationToken == KtTokens.ELVIS &&
            cast?.right != null &&
            cast.operationReference.getReferencedNameElementType() in CAST_TOKENS &&
            KtPsiUtil.deparenthesize(elvis.right) is KtReturnExpression

    context(session: KaSession)
    fun isStatement(): Boolean =
        with(session) {
            !elvis.isUsedAsExpression ||
                (
                    lambda?.run { functionLiteral.bodyExpression?.statements.orEmpty().lastOrNull() } == elvis &&
                        lambda.getStrictParentOfType<KtCallExpression>()?.isUsedAsExpression == false
                    )
        }

    private companion object {
        val CAST_TOKENS = setOf(KtTokens.AS_KEYWORD, KtTokens.AS_SAFE)
    }
}
