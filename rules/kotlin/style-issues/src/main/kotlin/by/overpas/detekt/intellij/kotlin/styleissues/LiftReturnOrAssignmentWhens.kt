package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.psi.KtWhenExpression

context(session: KaSession)
internal fun KtWhenExpression.hasLiftMissingCases(): Boolean {
    val subjectType = subjectExpression?.let { with(session) { it.expressionType?.withNullability(false) } }
    val subjectClass = with(session) { subjectType?.expandedSymbol }
    val isExhaustiveByType = subjectClass?.classKind == KaClassKind.ENUM_CLASS ||
        subjectClass?.modality == KaSymbolModality.SEALED ||
        with(session) { subjectType?.isBooleanType == true }
    return elseExpression == null && !isExhaustiveByType
}
