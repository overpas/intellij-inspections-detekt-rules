package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotation
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotationValue

internal fun KaAnnotation.isMissingReplaceWith(): Boolean {
    val replaceWith = arguments.firstOrNull { it.name.asString() == "replaceWith" }?.expression
    val level = arguments.firstOrNull { it.name.asString() == "level" }?.expression
    val replaceWithArguments = (replaceWith as? KaAnnotationValue.NestedAnnotationValue)?.run { annotation.arguments }
    val levelName = (level as? KaAnnotationValue.EnumEntryValue)?.callableId?.run { callableName.asString() }
    return replaceWithArguments.isNullOrEmpty() && levelName != "HIDDEN"
}
