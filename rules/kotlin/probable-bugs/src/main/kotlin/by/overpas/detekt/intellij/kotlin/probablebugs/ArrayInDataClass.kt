package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass

@IntellijInspection("ArrayInDataClass")
class ArrayInDataClass(config: Config) :
    Rule(
        config,
        "The generated `equals()` and `hashCode()` of a data or value class compare an array property by reference. " +
            "Override both functions to compare the array contents.",
    ),
    RequiresAnalysisApi {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        val kind = klass.arrayInDataClassKind() ?: return
        analyze(klass) { klass.arrayInDataClassParameters() }.forEach { parameter ->
            report(
                Finding(
                    Entity.from(parameter),
                    "Property with 'Array' type in a '$kind': " +
                        "it is recommended to override 'equals()' and 'hashCode()'",
                ),
            )
        }
    }
}
