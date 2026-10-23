package by.overpas.detekt.intellij.kotlin.styleissues.convertsealedclasstosealedinterface

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry

private val CONVERT_SEALED_CLASS_NON_INTERFACE_ANNOTATIONS =
    setOf("kotlin/jvm/Synchronized", "kotlin/jvm/JvmOverloads", "kotlin/jvm/JvmName").map(ClassId::fromString)

internal fun KtClass.hasSealedInterfaceShape(): Boolean {
    val constructor = primaryConstructor
    val hasConstructorState = constructor != null &&
        (constructor.valueParameters.isNotEmpty() || constructor.annotationEntries.isNotEmpty())
    val hasSuperTypeState = superTypeListEntries.any { it is KtSuperTypeCallEntry || it is KtDelegatedSuperTypeEntry }
    val hasBodyState = body?.let { body ->
        body.anonymousInitializers.isNotEmpty() ||
            body.properties.any { it.hasSealedClassState() } ||
            body.functions.any { it.hasModifier(KtTokens.FINAL_KEYWORD) } ||
            (body.functions + body.properties).any { declaration ->
                declaration.hasModifier(KtTokens.INTERNAL_KEYWORD) ||
                    declaration.hasModifier(KtTokens.PROTECTED_KEYWORD) ||
                    declaration.hasNonInterfaceModifier()
            }
    } == true
    return !hasConstructorState && secondaryConstructors.isEmpty() && !hasSuperTypeState && !hasBodyState
}

context(session: KaSession)
internal fun KtClass.hasNonInterfaceAnnotation(): Boolean =
    body?.run { functions + properties }.orEmpty().any { declaration ->
        declaration.annotationEntries.any { entry ->
            val type = with(session) { entry.typeReference?.run { type.fullyExpandedType } }
            (type as? KaClassType)?.classId in CONVERT_SEALED_CLASS_NON_INTERFACE_ANNOTATIONS
        }
    }

private fun KtCallableDeclaration.hasNonInterfaceModifier(): Boolean {
    val owners = listOf<KtModifierListOwner>(this) + (this as? KtProperty)?.accessors.orEmpty()
    val isVirtual = !hasModifier(KtTokens.PRIVATE_KEYWORD)
    return owners.any { owner ->
        owner.hasModifier(KtTokens.EXTERNAL_KEYWORD) ||
            (isVirtual && (owner.hasModifier(KtTokens.INLINE_KEYWORD) || owner.hasModifier(KtTokens.TAILREC_KEYWORD)))
    }
}

private fun KtProperty.hasSealedClassState(): Boolean =
    hasModifier(KtTokens.FINAL_KEYWORD) ||
        hasModifier(KtTokens.LATEINIT_KEYWORD) ||
        initializer != null ||
        (!hasModifier(KtTokens.ABSTRACT_KEYWORD) && getter == null)
