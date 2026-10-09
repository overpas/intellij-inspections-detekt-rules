package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression

context(session: KaSession)
internal fun KtCallExpression.appendsCharArrayRange(): Boolean =
    with(session) {
        val types = resolveToCall()
            ?.successfulFunctionCallOrNull()
            ?.run { symbol.valueParameters.map { it.returnType.lowerBoundIfFlexible() } }
            .orEmpty()
        types.size == 3 &&
            types[0].isArrayOrPrimitiveArray &&
            types[0].arrayElementType?.isCharType == true &&
            types[1].isIntType &&
            types[2].isIntType
    }
