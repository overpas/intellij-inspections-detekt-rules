package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
internal fun KtProperty.conflictsWithSyntheticProperty(): Boolean =
    with(session) {
        val name = nameAsName
        symbol.deprecationStatus?.deprecationLevel != DeprecationLevelValue.HIDDEN &&
            receiverTypeReference?.run { type.syntheticJavaPropertiesScope }
                ?.run {
                    getCallableSignatures { it == name }.any { it.symbol is KaSyntheticJavaPropertySymbol }
                } == true
    }
