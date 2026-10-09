package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.StandardClassIds

internal enum class ConvertTwoComparisonsToRangeCheckKind {
    SIGNED,
    UNSIGNED,
    FLOATING,
    CHAR,
    OTHER,
    ;

    val isIntegralOrChar: Boolean
        get() = this == SIGNED || this == UNSIGNED || this == CHAR

    companion object {

        private val kinds = mapOf(
            StandardClassIds.Int to SIGNED,
            StandardClassIds.Long to SIGNED,
            StandardClassIds.Short to SIGNED,
            StandardClassIds.Byte to SIGNED,
            StandardClassIds.UInt to UNSIGNED,
            StandardClassIds.ULong to UNSIGNED,
            StandardClassIds.UShort to UNSIGNED,
            StandardClassIds.UByte to UNSIGNED,
            StandardClassIds.Float to FLOATING,
            StandardClassIds.Double to FLOATING,
            StandardClassIds.Char to CHAR,
        )

        fun from(type: KaType): ConvertTwoComparisonsToRangeCheckKind =
            kinds[(type as? KaClassType)?.classId] ?: OTHER
    }
}
