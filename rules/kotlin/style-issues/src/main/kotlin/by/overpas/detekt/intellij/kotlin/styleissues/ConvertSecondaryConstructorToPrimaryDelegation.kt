package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtSecondaryConstructor

internal class ConvertSecondaryConstructorToPrimaryDelegation(private val target: KtSecondaryConstructor) {

    private val KtSecondaryConstructor.delegate: KtSecondaryConstructor?
        get() = analyze(this) {
            val call = getDelegationCall().resolveToCall()?.successfulFunctionCallOrNull()
            call?.run { symbol.psi } as? KtSecondaryConstructor
        }

    fun isReachableFromAll(constructors: List<KtSecondaryConstructor>): Boolean =
        constructors.all { start ->
            generateSequence(start) { it.delegate }.take(constructors.size + 1).any { it == target }
        }
}
