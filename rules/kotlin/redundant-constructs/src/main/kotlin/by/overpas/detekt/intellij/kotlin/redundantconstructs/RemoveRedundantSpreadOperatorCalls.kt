package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.psi.psiUtil.startOffset

private val ARRAY_FACTORY_NAMES = setOf(
    "arrayOf",
    "emptyArray",
    "booleanArrayOf",
    "byteArrayOf",
    "charArrayOf",
    "shortArrayOf",
    "intArrayOf",
    "longArrayOf",
    "floatArrayOf",
    "doubleArrayOf",
)

internal fun KtCallExpression.isInPlaceArrayFactoryCall(): Boolean {
    val callableId = analyze(this) { resolveToCall()?.singleFunctionCallOrNull()?.run { symbol.callableId } }
    return callableId?.packageName == StandardNames.BUILT_INS_PACKAGE_FQ_NAME &&
        callableId.callableName.asString() in ARRAY_FACTORY_NAMES
}

internal fun KtCallExpression.spreadCallTarget(): Any? =
    analyze(this) {
        resolveToCall()?.successfulFunctionCallOrNull()?.let { call ->
            call.symbol.psi ?: (call.symbol.callableId to call.symbol.valueParameters.map { it.name to it.isVararg })
        }
    }

internal fun KtCallExpression.textWithoutSpread(argument: KtValueArgument): String {
    val outer = getQualifiedExpressionForSelectorOrThis()
    val argumentList = valueArgumentList ?: return outer.text
    val inner = (argument.getArgumentExpression() as? KtCallExpression)
        ?.valueArguments
        .orEmpty()
        .map { it.asElement().text }
    val arguments = argumentList.arguments.flatMap { if (it == argument) inner else listOf(it.text) }
    val start = argumentList.startOffset - outer.startOffset
    return outer.text.replaceRange(
        start,
        start + argumentList.textLength,
        arguments.joinToString(prefix = "(", postfix = ")"),
    )
}
