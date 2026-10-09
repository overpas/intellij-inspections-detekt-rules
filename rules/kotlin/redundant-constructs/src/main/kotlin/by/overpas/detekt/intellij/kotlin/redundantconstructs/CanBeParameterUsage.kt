package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassInitializer
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class CanBeParameterUsage(
    private val name: KtSimpleNameExpression,
    private val klass: KtClass,
) {

    fun isPropertyAccess(): Boolean {
        val parent = name.parent
        val isSelector = parent is KtQualifiedExpression && parent.selectorExpression == name
        val isAssigned = parent is KtBinaryExpression && parent.left == name && KtPsiUtil.isAssignment(parent)
        return isSelector || isAssigned || !isInInitialization()
    }

    private fun isInInitialization(): Boolean {
        val user = name.parents.firstOrNull { parent ->
            USER_TYPES.any { it.isInstance(parent) } && !(parent is KtProperty && parent.isLocal)
        }
        return when (user) {
            is KtProperty -> user.containingClassOrObject === klass
            is KtClassInitializer -> user.containingDeclaration === klass
            is KtSuperTypeCallEntry -> user.getStrictParentOfType<KtClassOrObject>() === klass
            else -> false
        }
    }

    private companion object {
        val USER_TYPES = listOf(
            KtProperty::class,
            KtPropertyAccessor::class,
            KtClassInitializer::class,
            KtFunction::class,
            KtObjectDeclaration::class,
            KtSuperTypeCallEntry::class,
        )
    }
}
