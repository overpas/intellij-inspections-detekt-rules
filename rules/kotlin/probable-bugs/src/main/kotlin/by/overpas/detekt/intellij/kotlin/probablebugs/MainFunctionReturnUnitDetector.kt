package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtNamedFunction

internal class MainFunctionReturnUnitDetector(private val function: KtNamedFunction) {

    private val candidate = MainFunctionReturnUnitCandidate(function)

    private val isPreferredEntryPoint: Boolean
        get() = candidate.hasParameter ||
            function.containingKtFile.declarations
                .filterIsInstance<KtNamedFunction>()
                .map { MainFunctionReturnUnitCandidate(it) }
                .none { it.hasParameter && it.isEntryPoint }

    private val isReturningUnit: Boolean
        get() = analyze(function) { (function.symbol.returnType as? KaClassType)?.classId == StandardClassIds.Unit }

    val isMainNotReturningUnit: Boolean
        get() = candidate.isEntryPoint && isPreferredEntryPoint && !isReturningUnit
}
