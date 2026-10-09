package by.overpas.detekt.intellij.kotlin.codemigration

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression

private const val CONVERT_LONG_TO_DURATION_COROUTINES = "kotlinx.coroutines"

private const val CONVERT_LONG_TO_DURATION_FLOW = "$CONVERT_LONG_TO_DURATION_COROUTINES.flow"

private val CONVERT_LONG_TO_DURATION_PACKAGES = mapOf(
    "delay" to CONVERT_LONG_TO_DURATION_COROUTINES,
    "withTimeout" to CONVERT_LONG_TO_DURATION_COROUTINES,
    "withTimeoutOrNull" to CONVERT_LONG_TO_DURATION_COROUTINES,
    "debounce" to CONVERT_LONG_TO_DURATION_FLOW,
    "sample" to CONVERT_LONG_TO_DURATION_FLOW,
    "throttle" to CONVERT_LONG_TO_DURATION_FLOW,
    "timeout" to CONVERT_LONG_TO_DURATION_FLOW,
    "onTimeout" to "$CONVERT_LONG_TO_DURATION_COROUTINES.selects",
    "advanceTimeBy" to "$CONVERT_LONG_TO_DURATION_COROUTINES.test",
    "retryWhen" to CONVERT_LONG_TO_DURATION_FLOW,
)

private val CONVERT_LONG_TO_DURATION_CALLABLE_IDS = CONVERT_LONG_TO_DURATION_PACKAGES
    .map { (name, packageName) -> CallableId(FqName(packageName), Name.identifier(name)) }
    .toSet()

internal val CONVERT_LONG_TO_DURATION_FUNCTIONS = CONVERT_LONG_TO_DURATION_PACKAGES.keys

context(session: KaSession)
internal fun KtCallExpression.isConvertLongToDurationCall(): Boolean {
    val function = with(session) { resolveToCall()?.successfulFunctionCallOrNull()?.symbol }
    val firstParameterType = function?.valueParameters?.firstOrNull()?.returnType
    return function?.callableId in CONVERT_LONG_TO_DURATION_CALLABLE_IDS &&
        firstParameterType?.let { with(session) { it.isLongType } } == true
}
