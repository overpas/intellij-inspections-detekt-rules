package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal class UsePropertyAccessSyntaxProperty(
    val name: String,
    private val context: KtExpression,
    private val receiver: ClassId?,
) {

    fun isResolvedBy(text: String): Boolean {
        val content = KtPsiFactory(context.project).createExpressionCodeFragment(text, context).getContentElement()
        val access = (content as? KtBinaryExpression)?.left ?: content
        val reference = (access as? KtQualifiedExpression)?.selectorExpression ?: access
        return reference != null && analyze(reference) {
            val call = reference.resolveToCall()?.successfulVariableAccessCall()
            call?.symbol is KaSyntheticJavaPropertySymbol &&
                (access is KtQualifiedExpression || (call.dispatchReceiver?.type as? KaClassType)?.classId == receiver)
        }
    }
}
