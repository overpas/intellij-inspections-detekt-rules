package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

context(session: KaSession)
internal fun KtCallExpression.notThrownThrowableMessage(): String? =
    with(session) {
        val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
        val type = symbol?.returnType
        val callee = calleeExpression?.text
        when {
            symbol == null || type == null || callee == null -> null
            type.isNothingType || type.isNullable || !type.isSubtypeOf(builtinTypes.throwable) -> null
            isThrowableUsed() -> null
            symbol is KaConstructorSymbol -> "Throwable instance '$callee' is not thrown"
            else -> "Result of '$callee' call is not thrown"
        }
    }

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
private fun KtExpression.isThrowableUsed(): Boolean =
    with(session) {
        val holder = parents.firstOrNull { it is KtThrowExpression || it is KtReturnExpression || it is KtProperty }
        val property = holder as? KtProperty
        isUsedAsExpression &&
            (isUsedAsResultOfLambda || property == null || !property.isLocal || property.hasLocalReferences())
    }
