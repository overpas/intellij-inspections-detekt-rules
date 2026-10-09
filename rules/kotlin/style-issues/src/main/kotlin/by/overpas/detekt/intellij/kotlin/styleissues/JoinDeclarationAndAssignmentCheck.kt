package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.siblings

internal class JoinDeclarationAndAssignmentCheck(
    private val session: KaSession,
    private val property: KtProperty,
    private val assignment: KtBinaryExpression,
) {

    fun isJoinable(): Boolean =
        with(session) {
            val initializer = assignment.right
            val propertySymbol = property.symbol
            val nextSiblings = property.siblings(forward = true, withItself = false).toSet()
            val initializerSymbols = initializer?.joinReferencedSymbols().orEmpty()
            val secondaryConstructor = assignment.getStrictParentOfType<KtSecondaryConstructor>()
            val isSecondaryConstructorUsed = !property.isLocal &&
                assignment.parent != property.parent &&
                secondaryConstructor != null &&
                initializerSymbols.any {
                    it.psi?.getStrictParentOfType<KtSecondaryConstructor>() == secondaryConstructor
                }
            initializer != null &&
                !isSecondaryConstructorUsed &&
                assignment.joinAssignedReference()?.run { joinReferencedSymbols().singleOrNull() } == propertySymbol &&
                propertySymbol !in initializerSymbols &&
                initializerSymbols.none { it.psi in nextSiblings } &&
                property.hasJoinableTypeWith(initializer) &&
                !property.isLateinitUsedBefore(assignment)
        }
}
