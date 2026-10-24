package by.overpas.detekt.intellij.kotlin.otherproblems.deprecatedcallableaddreplacewith

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.util.OperatorNameConventions

private val DELEGATE_CONVENTIONS = setOf(
    OperatorNameConventions.GET_VALUE,
    OperatorNameConventions.SET_VALUE,
    OperatorNameConventions.PROVIDE_DELEGATE,
)

internal fun KtCallableDeclaration.deprecatedAnnotationOrNull(): KtAnnotationEntry? =
    annotationEntries.firstOrNull { it.shortName?.asString() == "Deprecated" }

internal fun KtNamedFunction.isDelegateConvention(): Boolean =
    hasModifier(KtTokens.OPERATOR_KEYWORD) && nameAsName in DELEGATE_CONVENTIONS

context(session: KaSession)
internal fun KtCallableDeclaration.canSuggestReplaceWith(): Boolean {
    val body = (this as? KtNamedFunction) ?: (this as? KtProperty)?.getter
    return with(session) {
        val annotation = symbol.annotations.firstOrNull { it.classId == StandardClassIds.Annotations.Deprecated }
        val isUnitReturned = (symbol as? KaCallableSymbol)?.run { returnType.isUnitType } == true
        val replacement = body?.replacementExpression(isUnitReturned)
        annotation?.isMissingReplaceWith() == true && replacement?.isReplaceWithCandidate() == true
    }
}
