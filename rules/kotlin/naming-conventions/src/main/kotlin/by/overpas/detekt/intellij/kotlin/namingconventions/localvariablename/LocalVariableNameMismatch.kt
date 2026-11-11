package by.overpas.detekt.intellij.kotlin.namingconventions.localvariablename

internal const val DEFAULT_LOCAL_VARIABLE_NAME_PATTERN = "[a-z][A-Za-z\\d]*"

internal fun String.localVariableNameMismatch(nameRegex: Regex): String? =
    when {
        this == "_" || nameRegex.matches(this) -> null
        nameRegex.pattern != DEFAULT_LOCAL_VARIABLE_NAME_PATTERN -> "doesn't match regex '${nameRegex.pattern}'"
        !first().isLowerCase() -> "should start with a lowercase letter"
        '_' in this -> "should not contain underscores"
        else -> "may contain only letters and digits"
    }
