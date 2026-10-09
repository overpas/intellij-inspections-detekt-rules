package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtNamedFunction

@IntellijInspection("MainFunctionReturnUnit")
class MainFunctionReturnUnit(config: Config) :
    Rule(
        config,
        "A `main` function that does not return `Unit` is not an entry point of the program. " +
            "Change its return type to `Unit`.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.hasDeclaredReturnType() && function.hasBlockBody()) return
        if (MainFunctionReturnUnitDetector(function).isMainNotReturningUnit) {
            report(Finding(Entity.from(function.typeReference ?: function), "'main()' should return Unit"))
        }
    }
}
