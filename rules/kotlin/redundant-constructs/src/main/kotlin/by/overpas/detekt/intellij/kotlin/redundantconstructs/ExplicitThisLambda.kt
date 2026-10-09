package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.types.Variance

internal class ExplicitThisLambda(private val reference: KtCallableReferenceExpression) {

    fun qualifiedCall(): KtDotQualifiedExpression? {
        val text = analyze(reference) { lambdaText() }.orEmpty()
        val fragment = KtPsiFactory(reference.project).createExpressionCodeFragment(text, reference)
        val body = (fragment.getContentElement() as? KtLambdaExpression)?.bodyExpression
        return body?.statements.orEmpty().singleOrNull() as? KtDotQualifiedExpression
    }

    @OptIn(KaExperimentalApi::class)
    private fun KaSession.lambdaText(): String? {
        val receiver = reference.receiverExpression?.text.orEmpty()
        val callee = "$receiver.${reference.callableReference.getReferencedName()}"
        val symbol = reference.callableReference.references
            .filterIsInstance<KtReference>()
            .firstNotNullOfOrNull { it.resolveToSymbol() }
        val parameters = (symbol as? KaFunctionSymbol)?.valueParameters.orEmpty().mapIndexed { index, parameter ->
            "p$index: ${parameter.returnType.render(position = Variance.INVARIANT)}"
        }
        val arguments = parameters.indices.joinToString { "p$it" }
        return when {
            reference.receiverExpression == null || symbol == null -> null
            symbol is KaFunctionSymbol -> "{ ${parameters.joinToString()} -> $callee($arguments) }"
            else -> "{ $callee }"
        }
    }
}
