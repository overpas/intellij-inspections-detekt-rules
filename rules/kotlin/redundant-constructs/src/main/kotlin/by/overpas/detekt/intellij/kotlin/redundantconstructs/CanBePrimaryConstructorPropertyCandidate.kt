package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBackingField
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.hasActualModifier
import org.jetbrains.kotlin.psi.psiUtil.isExpectDeclaration

internal class CanBePrimaryConstructorPropertyCandidate(private val property: KtProperty) {

    private val containingClass = property.getStrictParentOfType<KtClassOrObject>() as? KtClass

    fun isMovableToConstructor(): Boolean {
        val klass = containingClass
        return klass != null &&
            !property.isLocal &&
            !property.hasDelegate() &&
            property.getter == null &&
            property.setter == null &&
            !property.hasModifier(KtTokens.LATEINIT_KEYWORD) &&
            property.children.none { it is KtBackingField } &&
            !klass.isInterface() &&
            !klass.isExpectDeclaration() &&
            klass.secondaryConstructors.isEmpty() &&
            klass.primaryConstructor?.hasActualModifier() != true
    }

    fun isAssignedFromSameParameter(): Boolean {
        val initializer = property.initializer as? KtNameReferenceExpression
        return initializer != null &&
            analyze(property) {
                val parameterSymbol = targetOf(initializer) as? KaValueParameterSymbol
                val parameter = parameterSymbol?.psi as? KtParameter
                val classSymbol = containingClass?.classSymbol
                parameterSymbol != null &&
                    parameter != null &&
                    classSymbol != null &&
                    property.nameAsName == parameterSymbol.name &&
                    hasTypeOf(parameterSymbol) &&
                    parameter.collectDescendantsOfType<KtReferenceExpression>().none { targetOf(it) == classSymbol }
            }
    }

    private fun KaSession.hasTypeOf(parameterSymbol: KaValueParameterSymbol): Boolean {
        val propertyType = property.symbol.returnType
        return if (parameterSymbol.isVararg) {
            propertyType.arrayElementType?.semanticallyEquals(parameterSymbol.returnType) == true
        } else {
            propertyType.semanticallyEquals(parameterSymbol.returnType)
        }
    }

    private fun KaSession.targetOf(expression: KtReferenceExpression): KaSymbol? =
        expression.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
}
