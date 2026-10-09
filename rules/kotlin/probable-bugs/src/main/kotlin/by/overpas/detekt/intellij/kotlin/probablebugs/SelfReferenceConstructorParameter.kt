package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtPrimaryConstructor

class SelfReferenceConstructorParameter(config: Config) :
    Rule(
        config,
        "A primary constructor with a non-null parameter of its own class type can never be called. " +
            "Make the parameter type nullable.",
    ),
    RequiresAnalysisApi {

    override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor) {
        super.visitPrimaryConstructor(constructor)
        val owner = constructor.getContainingClassOrObject()
        val isSelfReferencing = analyze(constructor) {
            val ownerSymbol = owner.symbol
            val parameter = constructor.valueParameters.firstOrNull { parameter ->
                (parameter.typeReference?.type as? KaClassType)?.symbol == ownerSymbol
            }
            val type = parameter?.typeReference?.type
            parameter != null && !parameter.isVarArg && type?.isMarkedNullable == false
        }
        if (isSelfReferencing) {
            report(
                Finding(Entity.from(constructor), "Constructor has non-null self reference parameter"),
            )
        }
    }
}
