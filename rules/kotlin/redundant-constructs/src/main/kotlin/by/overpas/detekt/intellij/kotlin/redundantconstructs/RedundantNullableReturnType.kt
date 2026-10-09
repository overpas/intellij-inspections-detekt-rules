package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class RedundantNullableReturnType(config: Config) :
    Rule(
        config,
        "A nullable return type of a declaration that never returns null is redundant. Make the type non-nullable.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        function.reportIfRedundant("'${function.nameAsSafeName}' always returns non-null type")
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (!property.isVar) property.reportIfRedundant("'${property.nameAsSafeName}' is always non-null type")
    }

    private fun KtCallableDeclaration.reportIfRedundant(message: String) {
        val questionMark = nullableReturnTypeQuestionMark() ?: return
        if (!isOverridableCallable() && !hasTransientBackingField() && hasOnlyNonNullReturnTypes()) {
            report(Finding(Entity.from(questionMark), message))
        }
    }
}
