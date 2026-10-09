package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.hasActualModifier

private val EXCLUDED_MODIFIERS = listOf(KtTokens.OVERRIDE_KEYWORD, KtTokens.OPERATOR_KEYWORD, KtTokens.INFIX_KEYWORD)
private val OVERRIDABLE_MODALITIES = setOf(KaSymbolModality.OPEN, KaSymbolModality.ABSTRACT)

internal class UnusedReceiverParameterUsage(
    private val session: KaSession,
    private val declaration: KtCallableDeclaration,
    private val receiver: KtTypeReference,
) {

    private val symbol = with(session) { declaration.symbol } as? KaCallableSymbol

    private val receiverSymbol = with(session) { receiver.type.expandedSymbol }

    private val receiverTypeParameters = receiver.typeParameters()

    private val labels = declaration.collectDescendantsOfType<KtThisExpression>().mapNotNull { it.getLabelName() }

    fun hasExcludedModifier(): Boolean =
        EXCLUDED_MODIFIERS.any { declaration.hasModifier(it) } ||
            declaration.hasActualModifier() ||
            symbol?.modality in OVERRIDABLE_MODALITIES

    fun isExempt(): Boolean =
        with(session) {
            val returnTypeParameters = declaration.typeReference?.typeParameters().orEmpty()
            declaration.expectedType != null ||
                receiverTypeParameters.any { it in returnTypeParameters } ||
                (receiverSymbol as? KaClassSymbol)?.classKind == KaClassKind.COMPANION_OBJECT
        }

    fun usesClassLabels(): Boolean =
        with(session) {
            receiverSymbol != null && symbol?.containingDeclaration == receiverSymbol && labels.isNotEmpty()
        }

    fun hasReceiverLabel(): Boolean =
        receiverSymbol?.name?.asString() in labels

    fun isReceiverUsed(): Boolean {
        val callable = symbol
        val receiverType = callable?.receiverParameter?.returnType
        if (callable == null || receiverType == null) return false
        val references = UnusedReceiverParameterReferences(
            session = session,
            symbol = callable,
            receiverType = receiverType,
            reifiedParameters = receiverTypeParameters.filterTo(mutableSetOf()) { it.isReified },
        )
        return declaration.anyDescendantOfType<KtElement> { element ->
            element != declaration &&
                (
                    references.usesThis(element) ||
                        references.usesClassLiteral(element) ||
                        references.usesOperator(element) ||
                        references.usesCall(element) ||
                        references.usesReifiedType(element)
                    )
        }
    }

    private fun KtTypeReference.typeParameters(): Set<KaTypeParameterSymbol> {
        val references = listOf(this) + collectDescendantsOfType<KtTypeReference>()
        return with(session) { references.mapNotNullTo(mutableSetOf()) { (it.type as? KaTypeParameterType)?.symbol } }
    }
}
