package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression

private val replaceAssociateFunctionNames = setOf(
    FqName("kotlin.collections.associate"),
    FqName("kotlin.collections.associateTo"),
    FqName("kotlin.sequences.associate"),
    FqName("kotlin.sequences.associateTo"),
)

internal fun KtDotQualifiedExpression.associateReplacement(): String? {
    val call = selectorExpression as? KtCallExpression ?: return null
    val lambda = call.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
    val literal = lambda?.run {
        functionLiteral.takeIf { literal ->
            literal.valueParameters.size <= 1 &&
                PsiTreeUtil.findChildrenOfType(literal, KtReturnExpression::class.java)
                    .all { it.getTargetLabel() == null }
        }
    }
    return literal?.let {
        analyze(call) {
            val fqName = call.resolveToCall()
                ?.successfulFunctionCallOrNull()
                ?.run { symbol.callableId?.asSingleFqName() }
            ReplaceAssociateFunctionLambda(this, literal).replacement
                ?.takeIf { fqName in replaceAssociateFunctionNames }
                ?.let { if (call.calleeExpression?.text == "associateTo") "${it}To" else it }
        }
    }
}
