package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtSecondaryConstructor

class ConvertSecondaryConstructorToPrimary(config: Config) :
    Rule(
        config,
        "A class without a primary constructor has a secondary constructor that every other constructor " +
            "delegates to. Convert it to the primary constructor.",
    ),
    RequiresAnalysisApi {

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
        super.visitSecondaryConstructor(constructor)
        val klass = constructor.getContainingClassOrObject()
        if (constructor.getDelegationCall().isCallToThis || klass.hasPrimaryConstructor()) return
        val delegation = ConvertSecondaryConstructorToPrimaryDelegation(constructor)
        if (delegation.isReachableFromAll(klass.secondaryConstructors)) {
            report(
                Finding(
                    Entity.from(constructor.getConstructorKeyword()),
                    "Secondary constructor should be converted to a primary one",
                ),
            )
        }
    }
}
