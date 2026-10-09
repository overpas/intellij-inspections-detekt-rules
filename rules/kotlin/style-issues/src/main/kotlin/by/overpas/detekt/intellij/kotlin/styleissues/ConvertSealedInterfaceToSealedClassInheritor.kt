package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry

context(session: KaSession)
internal fun KtClassOrObject.canExtendSealedClass(sealedInterface: KtClass): Boolean {
    val isPlainClass = this !is KtClass ||
        (!isInterface() && !isEnum() && (primaryConstructor != null || secondaryConstructors.isEmpty()))
    val superTypeTargets = superTypeListEntries.map { entry ->
        val type = with(session) { entry.typeReference?.run { type.fullyExpandedType } }
        entry to (type as? KaClassType)?.run { symbol.psi }
    }
    val hasAnotherSuperClass = superTypeTargets.any { (entry, target) ->
        entry is KtSuperTypeCallEntry && target != sealedInterface
    }
    val hasDelegationToSealedInterface = superTypeTargets.any { (entry, target) ->
        entry is KtDelegatedSuperTypeEntry && target == sealedInterface
    }
    return isPlainClass && !hasAnotherSuperClass && !hasDelegationToSealedInterface
}
