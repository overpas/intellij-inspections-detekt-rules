package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

internal fun KtCallableDeclaration.deprecatedAnnotationOrNull(): KtAnnotationEntry? =
    annotationEntries.firstOrNull { it.shortName?.asString() == "Deprecated" }

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
