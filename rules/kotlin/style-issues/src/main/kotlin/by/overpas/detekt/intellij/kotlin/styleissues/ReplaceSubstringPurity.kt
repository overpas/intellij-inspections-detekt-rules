package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

private val replaceSubstringCollectionFactories = setOf(
    "listOf",
    "setOf",
    "mapOf",
    "arrayOf",
    "emptyList",
    "emptySet",
    "emptyMap",
    "emptyArray",
    "mutableListOf",
    "mutableSetOf",
    "mutableMapOf",
    "arrayListOf",
    "hashSetOf",
    "hashMapOf",
    "linkedSetOf",
    "linkedMapOf",
)

context(session: KaSession)
internal fun KtExpression.isReplaceSubstringPure(): Boolean =
    when (val expression = KtPsiUtil.safeDeparenthesize(this)) {
        is KtSimpleNameExpression -> expression.isReplaceSubstringStableRef()

        is KtCallExpression ->
            expression.calleeExpression?.text in replaceSubstringCollectionFactories &&
                expression.valueArguments.all { it.getArgumentExpression()?.isReplaceSubstringPure() != false }

        is KtQualifiedExpression ->
            expression.receiverExpression.isReplaceSubstringPure() &&
                expression.selectorExpression?.isReplaceSubstringPure() != false

        is KtConstantExpression -> true

        is KtStringTemplateExpression -> expression.entries.all { it is KtLiteralStringTemplateEntry }

        else -> false
    }

context(session: KaSession)
private fun KtSimpleNameExpression.isReplaceSubstringStableRef(): Boolean {
    val symbol = with(session) {
        references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
    }
    return when (val target = symbol?.psi) {
        is KtProperty -> target.isLocal || (target.initializer != null && !target.isVar)
        is KtParameter -> !(target.hasValOrVar() && target.isMutable)
        else -> false
    }
}
