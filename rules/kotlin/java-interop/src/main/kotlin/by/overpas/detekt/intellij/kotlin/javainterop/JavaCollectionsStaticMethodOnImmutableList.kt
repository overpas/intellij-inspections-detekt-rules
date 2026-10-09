package by.overpas.detekt.intellij.kotlin.javainterop

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

@IntellijInspection("JavaCollectionsStaticMethodOnImmutableList")
class JavaCollectionsStaticMethodOnImmutableList(config: Config) :
    Rule(
        config,
        "A `java.util.Collections` mutator method throws on an immutable Kotlin list. Pass a mutable list instead.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val callExpression = expression.selectorExpression as? KtCallExpression ?: return
        val methodName = analyze(callExpression) { callExpression.mutatorOnImmutableList() } ?: return
        val argument = callExpression.valueArguments.first().text
        val message = "Call of Java mutator '$methodName' on immutable Kotlin collection '$argument'"
        report(Finding(Entity.from(callExpression.calleeExpression ?: callExpression), message))
    }
}
