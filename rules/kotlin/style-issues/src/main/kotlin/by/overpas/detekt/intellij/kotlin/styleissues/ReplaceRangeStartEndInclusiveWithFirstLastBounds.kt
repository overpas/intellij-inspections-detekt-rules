package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

private val unboxedRangeBoundReplacements = mapOf("start" to "first", "endInclusive" to "last")

private val unboxedRangeTypes = setOf(
    ClassId.fromString("kotlin/ranges/IntRange"),
    ClassId.fromString("kotlin/ranges/CharRange"),
    ClassId.fromString("kotlin/ranges/LongRange"),
    ClassId.fromString("kotlin/ranges/UIntRange"),
    ClassId.fromString("kotlin/ranges/ULongRange"),
)

internal fun KtDotQualifiedExpression.unboxedRangeBoundReplacement(): String? {
    val replacement = unboxedRangeBoundReplacements[selectorExpression?.text] ?: return null
    val isPrimitiveRange = analyze(this) {
        (receiverExpression.expressionType as? KaClassType)?.classId in unboxedRangeTypes
    }
    return replacement.takeIf { isPrimitiveRange }
}
