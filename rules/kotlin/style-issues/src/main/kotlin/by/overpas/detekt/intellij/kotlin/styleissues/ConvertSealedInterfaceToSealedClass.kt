package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject

class ConvertSealedInterfaceToSealedClass(config: Config) :
    Rule(
        config,
        "A sealed interface whose inheritors are all plain classes or objects without another superclass " +
            "can be a sealed class. Convert it to a sealed class.",
    ),
    RequiresAnalysisApi {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (!klass.isSealed() || !klass.isInterface()) return
        val isConvertible = analyze(klass) {
            val inheritors = (klass.symbol as? KaNamedClassSymbol)?.sealedClassInheritors.orEmpty()
            inheritors.all { (it.psi as? KtClassOrObject)?.canExtendSealedClass(klass) == true }
        }
        if (isConvertible) report(Finding(Entity.from(klass), "Sealed interface can be converted to sealed class"))
    }
}
