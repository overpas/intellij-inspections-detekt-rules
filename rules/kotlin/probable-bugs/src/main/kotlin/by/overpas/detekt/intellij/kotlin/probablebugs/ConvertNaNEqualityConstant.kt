package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtExpression

private val CONVERT_NAN_EQUALITY_NAN_NAMES = setOf(
    "kotlin.Double.Companion.NaN",
    "java.lang.Double.NaN",
    "kotlin.Float.Companion.NaN",
    "java.lang.Float.NaN",
)

internal val KtExpression.isConvertNaNEqualityConstant: Boolean
    get() = text.endsWith("NaN") &&
        analyze(this) {
            val callableId = resolveToCall()?.successfulVariableAccessCall()?.run { symbol.callableId }
            callableId?.run { asSingleFqName().asString() } in CONVERT_NAN_EQUALITY_NAN_NAMES
        }
