package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds

private val suspiciousPathIterableClassIds = setOf(StandardClassIds.Iterable, StandardClassIds.Sequence)

private val suspiciousPathContainerClassIds = setOf(StandardClassIds.Collection, StandardClassIds.Sequence)

internal val KaType.suspiciousPathClassId: ClassId?
    get() = ((this as? KaFlexibleType)?.upperBound ?: this).let { it as? KaClassType }?.classId

context(session: KaSession)
internal fun KaType.suspiciousPathElementClassId(): ClassId? {
    val types = listOf(this) + with(session) { allSupertypes }
    val iterable = types.firstOrNull { type -> type.suspiciousPathClassId in suspiciousPathIterableClassIds }
    val iterableType = (iterable as? KaFlexibleType)?.upperBound ?: iterable
    return (iterableType as? KaClassType)?.typeArguments?.firstOrNull()?.type?.suspiciousPathClassId
}

context(session: KaSession)
internal fun KaType.isSuspiciousPathContainer(): Boolean =
    suspiciousPathClassId in suspiciousPathIterableClassIds + StandardClassIds.Collection ||
        with(session) { allSupertypes }.any { type -> type.suspiciousPathClassId in suspiciousPathContainerClassIds }
