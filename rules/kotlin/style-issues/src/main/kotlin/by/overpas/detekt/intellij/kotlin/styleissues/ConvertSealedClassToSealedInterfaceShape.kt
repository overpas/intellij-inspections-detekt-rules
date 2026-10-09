package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry

private val CONVERT_SEALED_CLASS_SYNCHRONIZED_CLASS_ID = ClassId.fromString("kotlin/jvm/Synchronized")

internal fun KtClass.hasSealedInterfaceShape(): Boolean {
    val constructor = primaryConstructor
    val hasConstructorState = constructor != null &&
        (constructor.valueParameters.isNotEmpty() || constructor.annotationEntries.isNotEmpty())
    val hasSuperTypeState = superTypeListEntries.any { it is KtSuperTypeCallEntry || it is KtDelegatedSuperTypeEntry }
    val hasBodyState = body?.let { body ->
        body.anonymousInitializers.isNotEmpty() ||
            body.properties.any { it.hasSealedClassState() } ||
            body.functions.any { it.hasModifier(KtTokens.FINAL_KEYWORD) }
    } == true
    return !hasConstructorState && secondaryConstructors.isEmpty() && !hasSuperTypeState && !hasBodyState
}

context(session: KaSession)
internal fun KtClass.hasSynchronizedFunction(): Boolean =
    body?.functions.orEmpty().any { function ->
        function.annotationEntries.any { entry ->
            val type = with(session) { entry.typeReference?.run { type.fullyExpandedType } }
            (type as? KaClassType)?.classId == CONVERT_SEALED_CLASS_SYNCHRONIZED_CLASS_ID
        }
    }

private fun KtProperty.hasSealedClassState(): Boolean =
    hasModifier(KtTokens.FINAL_KEYWORD) ||
        hasModifier(KtTokens.LATEINIT_KEYWORD) ||
        initializer != null ||
        (!hasModifier(KtTokens.ABSTRACT_KEYWORD) && getter == null)
