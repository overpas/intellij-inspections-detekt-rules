package by.overpas.detekt.intellij.jvm.testframeworks.assertbetweeninconvertibletypes

import by.overpas.detekt.intellij.jvm.testframeworks.superClassIds
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds

internal fun KaClassSymbol.readOnlyClassId(): ClassId? =
    classId?.readOnly()

context(session: KaSession)
internal fun KaClassSymbol.readOnlyClassIds(): Set<ClassId> =
    (superClassIds() + listOfNotNull(classId)).map { it.readOnly() }.toSet()

private fun ClassId.readOnly(): ClassId =
    StandardClassIds.Collections.mutableCollectionToBaseCollection[this] ?: this
