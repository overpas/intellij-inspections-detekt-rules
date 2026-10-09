package by.overpas.detekt.intellij.kotlin.styleissues

internal enum class IntroduceWhenSubjectState {
    UNCONSTRAINED,
    AND_ONLY,
    OR_ONLY,
    INCOMPATIBLE,
    GUARDS_NOT_SUPPORTED,
    ;

    operator fun plus(other: IntroduceWhenSubjectState): IntroduceWhenSubjectState =
        when {
            this == GUARDS_NOT_SUPPORTED || other == GUARDS_NOT_SUPPORTED -> GUARDS_NOT_SUPPORTED
            this == INCOMPATIBLE || other == INCOMPATIBLE -> INCOMPATIBLE
            this == UNCONSTRAINED -> other
            other == UNCONSTRAINED || this == other -> this
            else -> INCOMPATIBLE
        }
}
