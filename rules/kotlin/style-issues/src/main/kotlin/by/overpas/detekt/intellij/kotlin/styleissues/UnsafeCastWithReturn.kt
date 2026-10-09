package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtVariableDeclaration

class UnsafeCastWithReturn(config: Config) :
    Rule(
        config,
        "An unsafe cast followed by `?: return` throws on a failed cast instead of returning. " +
            "Use the safe cast `as?`.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val cast = expression.left as? KtBinaryExpressionWithTypeRHS ?: return
        val isUnsafeCastWithReturn = cast.right != null &&
            cast.operationReference.getReferencedNameElementType() == KtTokens.AS_KEYWORD &&
            expression.operationToken == KtTokens.ELVIS &&
            KtPsiUtil.deparenthesize(expression.right) is KtReturnExpression &&
            (expression.parent is KtVariableDeclaration || expression.parent is KtValueArgument)
        if (isUnsafeCastWithReturn) {
            report(Finding(Entity.from(expression), "Replace the unsafe cast with the safe one"))
        }
    }
}
