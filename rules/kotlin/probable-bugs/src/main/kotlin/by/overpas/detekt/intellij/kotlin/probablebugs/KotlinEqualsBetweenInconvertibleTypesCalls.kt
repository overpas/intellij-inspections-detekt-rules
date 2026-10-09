package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.KaStandardTypeClassIds
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

context(session: KaSession)
internal fun KtCallExpression.isInconvertibleEqualsCall(): Boolean {
    val receiverType = getQualifiedExpressionForSelector()?.run { receiverExpression.equalsComparableType() }
    val argumentType = valueArguments.singleOrNull()?.getArgumentExpression()?.equalsComparableType()
    return receiverType != null &&
        argumentType != null &&
        with(session) { !receiverType.semanticallyEquals(argumentType) }
}

context(session: KaSession)
private fun KtExpression.equalsComparableType(): KaType? {
    val type = with(session) { expressionType?.withNullability(false) }
    val classId = (type as? KaClassType)?.classId
    val symbol = type?.let { with(session) { it.expandedSymbol } }
    val isComparable = classId in KaStandardTypeClassIds.PRIMITIVES ||
        classId == KaStandardTypeClassIds.STRING ||
        symbol?.classKind == KaClassKind.ENUM_CLASS
    return type?.takeIf { isComparable }
}
