package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.psi.KtTypeReference

internal class FoldInitializerAndIfToElvisTypeCheck(
    private val checkedTypeReference: KtTypeReference,
    private val superType: KaType,
) {

    @OptIn(KaExperimentalApi::class)
    context(session: KaSession)
    fun isPossible(): Boolean =
        with(session) {
            val checkedType = checkedTypeReference.type
            when {
                checkedType is KaTypeParameterType -> checkedType.hasCommonSubtypeWith(superType)

                superType is KaTypeParameterType ->
                    superType.symbol.upperBounds.all {
                        FoldInitializerAndIfToElvisTypeCheck(checkedTypeReference, it).isPossible()
                    }

                else ->
                    superType.expandedSymbol?.run {
                        checkedType.isSubtypeOf(defaultTypeWithStarProjections.withNullability(superType.isNullable))
                    } == true
            }
        }
}
