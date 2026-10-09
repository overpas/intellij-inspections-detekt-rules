package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaLocalVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class CanBeParameterCandidate(private val parameter: KtParameter) {

    private val klass = ((parameter.parent as? KtParameterList)?.parent as? KtPrimaryConstructor)
        ?.getContainingClassOrObject() as? KtClass

    fun isConstructorProperty(): Boolean =
        klass?.isData() == false &&
            !parameter.hasModifier(KtTokens.OVERRIDE_KEYWORD) &&
            !parameter.hasModifier(KtTokens.ACTUAL_KEYWORD)

    fun isVisibleOnlyInFile(): Boolean =
        parameter.hasModifier(KtTokens.PRIVATE_KEYWORD) ||
            parameter.parents.filterIsInstance<KtClassOrObject>()
                .any { it.isLocal || it.hasModifier(KtTokens.PRIVATE_KEYWORD) }

    fun isNeverUsedAsProperty(): Boolean {
        val owner = klass
        return owner != null &&
            analyze(parameter) {
                val parameterSymbol = parameter.symbol as? KaValueParameterSymbol
                val targets = setOfNotNull(parameterSymbol, parameterSymbol?.generatedPrimaryConstructorProperty)
                val usages = parameter.containingKtFile.collectDescendantsOfType<KtSimpleNameExpression> {
                    it.getReferencedName() == parameter.name && targetOf(it) in targets
                }
                usages.isNotEmpty() &&
                    usages.none { usage ->
                        usage.parent is KtCallableReferenceExpression ||
                            CanBeParameterUsage(usage, owner).isPropertyAccess()
                    } &&
                    !hasShadowingReference(targets)
            }
    }

    private fun KaSession.hasShadowingReference(targets: Set<KaSymbol>): Boolean {
        val properties = klass?.getProperties().orEmpty()
        val scopes = klass?.getAnonymousInitializers().orEmpty() +
            properties.mapNotNull { it.initializer } +
            properties.mapNotNull { it.delegate }
        return scopes.asSequence()
            .flatMap { scope ->
                scope.collectDescendantsOfType<KtSimpleNameExpression> {
                    it.getReferencedName() == parameter.name && !it.isQualifiedReceiver()
                }
            }
            .mapNotNull { targetOf(it) }
            .any { it !in targets && it !is KaLocalVariableSymbol }
    }

    private fun KtSimpleNameExpression.isQualifiedReceiver(): Boolean =
        parents.takeWhile { it is KtDotQualifiedExpression }
            .zip(sequenceOf(this) + parents)
            .any { (qualified, child) -> (qualified as KtDotQualifiedExpression).selectorExpression !== child }

    private fun KaSession.targetOf(expression: KtSimpleNameExpression): KaSymbol? =
        expression.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
}
