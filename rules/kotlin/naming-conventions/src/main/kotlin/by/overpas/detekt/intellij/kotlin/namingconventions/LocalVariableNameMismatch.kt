package by.overpas.detekt.intellij.kotlin.namingconventions

private val localVariableNamePattern = Regex("[a-z][A-Za-z\\d]*")

internal fun String.localVariableNameMismatch(): String? =
    when {
        this == "_" || localVariableNamePattern.matches(this) -> null
        !first().isLowerCase() -> "should start with a lowercase letter"
        '_' in this -> "should not contain underscores"
        else -> "may contain only letters and digits"
    }
