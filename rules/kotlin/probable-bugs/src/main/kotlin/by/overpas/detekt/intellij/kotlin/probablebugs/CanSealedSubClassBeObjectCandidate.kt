package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

internal class CanSealedSubClassBeObjectCandidate(private val klass: KtClass) {

    fun isSingletonShaped(): Boolean =
        klass.typeParameters.isEmpty() &&
            klass.companionObjects.isEmpty() &&
            klass.primaryConstructorParameters.isEmpty() &&
            klass.secondaryConstructors.all { it.valueParameters.isEmpty() } &&
            exclusivelyClassModifiers.none { klass.hasModifier(it) } &&
            klass.declarations.filterIsInstance<KtClass>().none { it.isInner() } &&
            klass.hasNoStateOrEquals()

    fun extendsStatelessSealed(): Boolean =
        analyze(klass) {
            (klass.symbol as? KaNamedClassSymbol)?.modality == KaSymbolModality.FINAL &&
                klass.superClasses().any { sealed ->
                    sealed.isSealed() &&
                        generateSequence(sealed) { it.superClasses().firstOrNull() }.all { it.hasNoStateOrEquals() }
                }
        }

    context(session: KaSession)
    private fun KtClass.superClasses(): List<KtClass> =
        superTypeListEntries.mapNotNull { entry ->
            with(session) { entry.typeReference?.type?.expandedSymbol?.psi } as? KtClass
        }

    private fun KtClass.hasNoStateOrEquals(): Boolean =
        primaryConstructorParameters.isEmpty() &&
            declarations.none { declaration ->
                (declaration is KtProperty && declaration.hasSealedSubClassState()) ||
                    (declaration is KtNamedFunction && declaration.isSealedSubClassEquality())
            }

    private companion object {
        val exclusivelyClassModifiers = listOf(
            KtTokens.ANNOTATION_KEYWORD,
            KtTokens.ENUM_KEYWORD,
            KtTokens.INNER_KEYWORD,
            KtTokens.SEALED_KEYWORD,
        )
    }
}
