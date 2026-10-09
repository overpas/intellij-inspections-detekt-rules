@file:OptIn(KaExperimentalApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

private val ARRAY_OF = CallableId(StandardClassIds.BASE_KOTLIN_PACKAGE, Name.identifier("arrayOf"))

internal class RemoveExplicitTypeArgumentsCall(private val call: KtCallExpression) {

    fun isRedundant(): Boolean {
        val arguments = call.typeArguments
        val hasPlainArguments = arguments.isNotEmpty() &&
            arguments.none { it.typeReference?.anyDescendantOfType<KtAnnotationEntry>() == true }
        val original = if (hasPlainArguments) analyze(call) { originalInfo() } else null
        val newCall = original?.let { call.withoutTypeArgumentsInFragment() }
        return original != null && newCall != null && RemoveExplicitTypeArgumentsComparison(original, newCall).matches()
    }

    private fun KaSession.originalInfo(): RemoveExplicitTypeArgumentsCallInfo? {
        val symbol = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
        val isInferable = symbol != null &&
            !symbol.isReifiedInline &&
            (symbol.receiverParameter != null || !call.hasUninferableTypeArguments)
        val types = if (isInferable) call.typeArgumentPointers() else null
        return types?.let { RemoveExplicitTypeArgumentsCallInfo(it, call.typeArgumentDiagnosticCount()) }
    }
}

private val KaFunctionSymbol.isReifiedInline: Boolean
    get() = this is KaNamedFunctionSymbol &&
        callableId != ARRAY_OF &&
        isInline &&
        typeParameters.any { it.isReified }

private val KtCallExpression.hasUninferableTypeArguments: Boolean
    get() {
        val property = parent as? KtProperty ?: (parent as? KtDotQualifiedExpression)?.parent as? KtProperty
        return valueArguments.isEmpty() && property != null && property.typeReference == null
    }
