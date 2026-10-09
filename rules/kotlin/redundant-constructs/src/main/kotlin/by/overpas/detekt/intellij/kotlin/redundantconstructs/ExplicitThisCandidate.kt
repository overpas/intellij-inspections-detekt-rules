package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaIdeApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.ShortenOptions
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal class ExplicitThisCandidate(private val expression: KtThisExpression) {

    fun isRedundant(): Boolean =
        when (val parent = expression.parent) {
            is KtDotQualifiedExpression -> parent.receiverExpression == expression && parent.canDropReceiver()

            is KtCallableReferenceExpression ->
                parent.receiverExpression == expression &&
                    ExplicitThisLambda(parent).qualifiedCall()?.canDropReceiver() == true

            else -> false
        }

    @OptIn(KaIdeApi::class)
    private fun KtDotQualifiedExpression.canDropReceiver(): Boolean {
        val qualified = this
        return analyze(qualified) {
            collectPossibleReferenceShortenings(
                file = qualified.containingKtFile,
                selection = qualified.textRange,
                shortenOptions = ShortenOptions(removeThis = true, removeThisLabels = true),
            ).listOfQualifierToShortenInfo.any { it.qualifierToShorten.element == qualified }
        }
    }
}
