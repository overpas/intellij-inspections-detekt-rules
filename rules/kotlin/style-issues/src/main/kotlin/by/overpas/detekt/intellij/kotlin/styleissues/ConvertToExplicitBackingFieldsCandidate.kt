package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

internal class ConvertToExplicitBackingFieldsCandidate(
    private val property: KtProperty,
    getter: KtPropertyAccessor,
) {

    private val returned = when (val body = getter.bodyExpression) {
        is KtNameReferenceExpression -> body

        is KtBlockExpression ->
            (body.statements.singleOrNull() as? KtReturnExpression)?.returnedExpression as? KtNameReferenceExpression

        is KtDotQualifiedExpression ->
            body.takeIf { it.receiverExpression is KtThisExpression }?.selectorExpression as? KtNameReferenceExpression

        else -> null
    }

    private val KtProperty.isPlainPrivateValue: Boolean
        get() = isPrivate() &&
            !isVar &&
            !hasDelegate() &&
            getter == null &&
            parent == property.parent &&
            this != property

    val isConvertible: Boolean
        get() {
            val reference = returned ?: return false
            return analyze(property) {
                val symbol = property.symbol
                val owner = symbol.containingDeclaration as? KaNamedClassSymbol
                val isEffectivelyFinal = symbol.modality == KaSymbolModality.FINAL ||
                    owner?.run { modality == KaSymbolModality.FINAL && classKind != KaClassKind.ENUM_CLASS } == true
                val backing = reference.references
                    .filterIsInstance<KtReference>()
                    .firstNotNullOfOrNull { it.resolveToSymbol() } as? KaPropertySymbol
                val backingType = backing?.takeIf { (it.psi as? KtProperty)?.isPlainPrivateValue == true }?.returnType
                isEffectivelyFinal &&
                    backingType != null &&
                    !backingType.semanticallyEquals(symbol.returnType) &&
                    backingType.isSubtypeOf(symbol.returnType)
            }
        }
}
