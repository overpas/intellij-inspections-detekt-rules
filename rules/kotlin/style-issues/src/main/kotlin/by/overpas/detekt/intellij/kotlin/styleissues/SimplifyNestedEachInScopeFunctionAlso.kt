package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

context(session: KaSession)
internal fun SimplifyNestedEachInScopeFunctionCall.isSimplifiableAlso(): Boolean =
    with(session) {
        val receiver = (statement as? KtDotQualifiedExpression)?.receiverExpression as? KtNameReferenceExpression
        val parameter = receiver?.run {
            references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        } as? KaValueParameterSymbol
        parameter != null &&
            parameter.containingDeclaration?.psi == lambda.functionLiteral &&
            eachLambdaBody?.anyDescendantOfType<KtNameReferenceExpression> { reference ->
                reference.references.filterIsInstance<KtReference>().any { it.resolveToSymbol() == parameter }
            } != true
    }
