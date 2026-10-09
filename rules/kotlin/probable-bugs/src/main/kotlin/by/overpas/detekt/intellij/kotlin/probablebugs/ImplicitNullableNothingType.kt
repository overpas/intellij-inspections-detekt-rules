package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("ImplicitNullableNothingType")
class ImplicitNullableNothingType(config: Config) :
    Rule(
        config,
        "A `var` or an open declaration without an explicit type whose inferred type is `Nothing?` can only hold " +
            "or return null. Specify the intended type explicitly.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        function.reportIfNullableNothing()
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        property.reportIfNullableNothing()
    }

    private fun KtCallableDeclaration.reportIfNullableNothing() {
        if (hasImplicitNullableNothingType()) report(Finding(Entity.atName(this), "Implicit 'Nothing?' type"))
    }
}
