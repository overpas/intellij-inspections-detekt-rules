@file:OptIn(KaExperimentalApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaDefinitelyNotNullType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.psi.KtCallExpression

internal class RemoveExplicitTypeArgumentsComparison(
    private val original: RemoveExplicitTypeArgumentsCallInfo,
    private val newCall: KtCallExpression,
) {

    fun matches(): Boolean {
        val newTypes = newCall.typeArgumentPointers()
        val newDiagnosticCount = newCall.typeArgumentDiagnosticCount()
        return newTypes != null &&
            original.types.size == newTypes.size &&
            (newDiagnosticCount == 0 || newDiagnosticCount == original.diagnosticCount) &&
            analyze(newCall) {
                original.types.zip(newTypes).all { (old, new) -> isSameType(old.restore(), new.restore()) }
            }
    }

    private fun KaSession.isSameType(
        oldType: KaType?,
        newType: KaType?,
    ): Boolean {
        val left = if (oldType is KaDefinitelyNotNullType &&
            newType is KaDefinitelyNotNullType
        ) {
            oldType.original
        } else {
            oldType
        }
        val right = if (oldType is KaDefinitelyNotNullType &&
            newType is KaDefinitelyNotNullType
        ) {
            newType.original
        } else {
            newType
        }
        return when {
            left == null || right == null -> false
            left is KaTypeParameterType && right is KaTypeParameterType -> left.name == right.name
            else -> left.hasFlexibleNullability == right.hasFlexibleNullability && left.semanticallyEquals(right)
        }
    }
}
