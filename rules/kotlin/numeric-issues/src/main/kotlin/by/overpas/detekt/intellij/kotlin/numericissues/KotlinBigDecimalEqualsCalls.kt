package by.overpas.detekt.intellij.kotlin.numericissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression

internal const val KOTLIN_BIG_DECIMAL_EQUALS_NAME = "equals"

private val BIG_DECIMAL = ClassId.fromString("java/math/BigDecimal")

private val BIG_DECIMAL_EQUALS = CallableId(BIG_DECIMAL, Name.identifier(KOTLIN_BIG_DECIMAL_EQUALS_NAME))

context(session: KaSession)
internal fun KtBinaryExpression.isBigDecimalEquality(): Boolean =
    left.isBigDecimal() && right.isBigDecimal() && operationReference.callsBigDecimalEquals()

context(session: KaSession)
internal fun KtCallExpression.isBigDecimalEqualsCall(): Boolean =
    callsBigDecimalEquals() && valueArguments.single().getArgumentExpression().isBigDecimal()

context(session: KaSession)
private fun KtExpression?.isBigDecimal(): Boolean {
    val type = with(session) { this@isBigDecimal?.expressionType?.upperBoundIfFlexible() }
    return (type as? KaClassType)?.classId == BIG_DECIMAL
}

context(session: KaSession)
private fun KtElement.callsBigDecimalEquals(): Boolean =
    with(session) { resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } } == BIG_DECIMAL_EQUALS
