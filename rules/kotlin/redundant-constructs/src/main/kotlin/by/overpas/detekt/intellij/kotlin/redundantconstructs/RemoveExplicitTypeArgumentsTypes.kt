@file:OptIn(KaExperimentalApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypePointer
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal fun KtCallExpression.typeArgumentPointers(): List<KaTypePointer<KaType>>? =
    analyze(this) {
        resolveToCall()
            ?.singleFunctionCallOrNull()
            ?.run { typeArgumentsMapping.values.map { it.createPointer() } }
    }

internal fun KtCallExpression.typeArgumentDiagnosticCount(): Int =
    analyze(this) {
        (collectDescendantsOfType<KtElement>() + this@typeArgumentDiagnosticCount).sumOf { element ->
            element.directDiagnostics(KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS).count { diagnostic ->
                diagnostic is KaFirDiagnostic.UnresolvedReference ||
                    diagnostic is KaFirDiagnostic.BuilderInferenceStubReceiver ||
                    diagnostic is KaFirDiagnostic.ImplicitNothingReturnType ||
                    diagnostic is KaFirDiagnostic.AmbiguousContextArgument
            }
        }
    }
