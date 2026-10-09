package by.overpas.detekt.intellij.kotlin.probablebugs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.contracts.description.KaContractCallsInPlaceContractEffectDeclaration
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

private val atMostOnceOccurrences = setOf(
    EventOccurrencesRange.AT_MOST_ONCE,
    EventOccurrencesRange.EXACTLY_ONCE,
)

internal data class AmbiguousNonLocalJumpCallee(
    val name: String,
    val isInSource: Boolean,
) {

    val advice: String
        get() = if (isInSource) {
            "Use clarifying label or add 'callsInPlace' contract to '$name'."
        } else {
            "Use clarifying label."
        }
}

internal fun PsiElement.ambiguousNonLocalJumpCall(): KtCallExpression? {
    val argument = takeIf { it is KtLambdaExpression || it is KtNamedFunction }?.parent as? KtValueArgument
    val owner = if (argument is KtLambdaArgument) {
        argument.parent
    } else {
        argument?.parent?.takeIf { it is KtValueArgumentList }?.parent
    }
    return owner as? KtCallExpression
}

@OptIn(KaExperimentalApi::class)
internal fun KaNamedFunctionSymbol.hasNoCallsInPlaceContract(parameterName: Name): Boolean =
    contractEffects.none { effect ->
        effect is KaContractCallsInPlaceContractEffectDeclaration &&
            (effect.valueParameterReference.symbol as? KaValueParameterSymbol)?.name == parameterName &&
            effect.occurrencesRange in atMostOnceOccurrences
    }
