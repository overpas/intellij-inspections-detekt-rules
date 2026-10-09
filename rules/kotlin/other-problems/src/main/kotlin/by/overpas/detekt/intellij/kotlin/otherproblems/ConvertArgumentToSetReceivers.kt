package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds

private val setConversionSequenceClassId = ClassId.fromString("kotlin/sequences/Sequence")

private val intersectSubtractReceivers = setOf(
    StandardClassIds.Array,
    ClassId.fromString("kotlin/ByteArray"),
    ClassId.fromString("kotlin/ShortArray"),
    ClassId.fromString("kotlin/IntArray"),
    ClassId.fromString("kotlin/LongArray"),
    ClassId.fromString("kotlin/FloatArray"),
    ClassId.fromString("kotlin/DoubleArray"),
    ClassId.fromString("kotlin/BooleanArray"),
    ClassId.fromString("kotlin/CharArray"),
    StandardClassIds.Iterable,
)

private val setConversionReceivers = mapOf(
    "minus" to setOf(
        StandardClassIds.Set,
        StandardClassIds.Iterable,
        StandardClassIds.Map,
        setConversionSequenceClassId,
    ),
    "minusAssign" to setOf(StandardClassIds.MutableCollection, StandardClassIds.MutableMap),
    "intersect" to intersectSubtractReceivers,
    "subtract" to intersectSubtractReceivers,
    "removeAll" to setOf(StandardClassIds.MutableCollection),
    "retainAll" to setOf(StandardClassIds.MutableCollection),
)

context(session: KaSession)
internal fun KaType.isSetConversionReceiverOf(functionName: String): Boolean =
    setConversionReceivers[functionName].orEmpty().any { classId -> with(session) { isSubtypeOf(classId) } }
