package by.overpas.detekt.intellij.kotlin.javainterop

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality

internal class JavaDefaultMethodsNotOverriddenByDelegationOverrides(
    private val session: KaSession,
    private val methods: Set<KaCallableSymbol>,
) {

    fun KaClassSymbol.overridesAnyMethod(): Boolean =
        when (modality) {
            KaSymbolModality.SEALED ->
                (this as? KaNamedClassSymbol)?.let { named -> with(session) { named.sealedClassInheritors } }.orEmpty()
                    .any { it.declaresOverride() }

            KaSymbolModality.FINAL -> declaresOverride()

            KaSymbolModality.ABSTRACT, KaSymbolModality.OPEN -> true
        }

    private fun KaClassSymbol.declaresOverride(): Boolean =
        with(session) {
            memberScope.callables.any { callable ->
                callable.fakeOverrideOriginal !in methods &&
                    callable.allOverriddenSymbols.any { it.fakeOverrideOriginal in methods }
            }
        }
}
