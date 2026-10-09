package by.overpas.detekt.intellij.kotlin.migration

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotationValue
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.analysis.api.symbols.KaDeclarationSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.name.StandardClassIds

private const val REPLACE_WITH_ARGUMENT = "replaceWith"

private const val EXPRESSION_ARGUMENT = "expression"

context(session: KaSession)
internal fun KaSymbol.hasDeprecationReplacement(): Boolean {
    val container = with(session) { containingSymbol }
    val deprecated = listOfNotNull(this, container)
        .filterIsInstance<KaDeclarationSymbol>()
        .firstOrNull { symbol -> symbol.annotations.any { it.classId == StandardClassIds.Annotations.Deprecated } }
    return deprecated?.hasReplaceWith() == true
}

internal fun KaDeclarationSymbol.hasReplaceWith(): Boolean {
    val replaceWith = annotations
        .firstOrNull { it.classId == StandardClassIds.Annotations.Deprecated }
        ?.arguments
        ?.firstOrNull { it.name.asString() == REPLACE_WITH_ARGUMENT }
        ?.expression as? KaAnnotationValue.NestedAnnotationValue
    val expression = replaceWith?.annotation?.arguments
        ?.firstOrNull { it.name.asString() == EXPRESSION_ARGUMENT }
        ?.expression as? KaAnnotationValue.ConstantValue
    return expression?.value is KaConstantValue.StringValue
}
