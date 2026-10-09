package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtQualifiedExpression

@IntellijInspection("UselessCallOnCollection")
class UselessCallOnCollection(config: Config) :
    Rule(
        config,
        "A filter or a not-null mapping call on a collection or a sequence whose elements already match has no " +
            "effect. Remove the call or use the simpler call.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        expression.uselessCallOnCollectionMessage()?.let { report(Finding(Entity.from(expression), it)) }
    }
}
