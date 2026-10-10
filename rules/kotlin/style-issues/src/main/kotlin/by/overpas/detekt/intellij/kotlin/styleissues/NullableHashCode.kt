package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("NullableHashCode")
class NullableHashCode(config: Config) :
    Rule(
        config,
        "A safe `hashCode()` call with a zero default is verbose. " +
            "Call the `hashCode()` extension on the nullable receiver instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val call = expression.nullableHashCodeCall() ?: return
        if (call.isNullableHashCodeCall()) {
            report(Finding(Entity.from(call.calleeExpression ?: call), "Nullable value 'hashCode()' can be simplified"))
        }
    }
}
