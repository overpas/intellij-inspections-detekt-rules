package by.overpas.detekt.intellij.kotlin.logging

import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal fun KtElement.loggerOwnerNames(): List<String> {
    val owner = getStrictParentOfType<KtClassOrObject>()
    return if (owner is KtObjectDeclaration && owner.isCompanion()) {
        listOfNotNull(owner.name, owner.containingClass()?.name)
    } else {
        listOfNotNull(owner?.name)
    }
}
