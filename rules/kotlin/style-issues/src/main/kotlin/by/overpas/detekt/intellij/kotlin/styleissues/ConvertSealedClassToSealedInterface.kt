package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject

@IntellijInspection("ConvertSealedClassToSealedInterface")
class ConvertSealedClassToSealedInterface(config: Config) :
    Rule(
        config,
        "A sealed class without state, constructor parameters or final members can be a sealed interface. " +
            "Convert it to a sealed interface.",
    ),
    RequiresAnalysisApi {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (!klass.isSealed() || klass.isInterface() || !klass.hasSealedInterfaceShape()) return
        val isConvertible = analyze(klass) {
            val symbol = klass.symbol as? KaNamedClassSymbol
            symbol != null &&
                !klass.hasSynchronizedFunction() &&
                symbol.sealedClassInheritors.all { it.psi is KtClassOrObject }
        }
        if (isConvertible) report(Finding(Entity.from(klass), "Sealed class can be converted to sealed interface"))
    }
}
