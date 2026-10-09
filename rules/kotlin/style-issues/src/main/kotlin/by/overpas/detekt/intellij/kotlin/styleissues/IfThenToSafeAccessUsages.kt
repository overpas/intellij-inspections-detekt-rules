package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private val SENSELESS_CONDITION_DIAGNOSTICS = setOf("SENSELESS_COMPARISON", "USELESS_IS_CHECK")

context(session: KaSession)
internal fun IfThenToSafeAccessData.checkedUsages(): List<KtExpression> {
    val target = checkedExpression.ifThenTargetSymbol()
    return baseClause.collectDescendantsOfType<KtExpression>({ it !is KtBlockExpression }) { usage ->
        target != null &&
            usage::class == checkedExpression::class &&
            usage.text == checkedExpression.text &&
            usage.ifThenTargetSymbol() == target
    }
}

context(session: KaSession)
internal fun KtExpression.ifThenSmartCastStability(): Boolean? =
    with(session) {
        val expression =
            (this@ifThenSmartCastStability as? KtThisExpression)?.instanceReference ?: this@ifThenSmartCastStability
        expression.smartCastInfo?.isStable
    }

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
internal fun KtExpression.isSenselessIfThenCondition(): Boolean =
    with(session) {
        directDiagnostics(KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS)
            .any { it.factoryName in SENSELESS_CONDITION_DIAGNOSTICS }
    }
