package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class VerboseNullabilityAndEmptinessCandidate(
    private val nullCheck: VerboseNullabilityAndEmptinessNullCheck,
    private val content: VerboseNullabilityAndEmptinessContent,
) {

    val replacement: String
        get() = (if (nullCheck.isPositive) "!" else "") + content.function.replacementName

    context(session: KaSession)
    fun isConfirmed(): Boolean =
        nullCheck.isPositive == content.isPositive &&
            nullCheck.target.matches(content.target) &&
            content.target.hasSmartCast() &&
            content.isApplicable()
}

internal fun KtBinaryExpression.verboseNullabilityCandidate(): VerboseNullabilityAndEmptinessCandidate? {
    val nullCheck = verboseNullabilityNullCheck()
    val content = nullCheck?.contentExpression()?.verboseNullabilityContent()
    val isApplicable = nullCheck != null &&
        content != null &&
        nullCheck.target.matchesByPsi(content.target) &&
        parents.none { it is KtNamedFunction && it.name == content.function.replacementName }
    return if (isApplicable) VerboseNullabilityAndEmptinessCandidate(nullCheck, content) else null
}
