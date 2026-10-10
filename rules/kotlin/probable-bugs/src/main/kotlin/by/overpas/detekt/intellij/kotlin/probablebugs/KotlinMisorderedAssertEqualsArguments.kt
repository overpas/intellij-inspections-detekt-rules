package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

internal val MISORDERED_ASSERT_METHOD_NAMES = setOf(
    "assertEquals",
    "assertEqualsNoOrder",
    "assertNotEquals",
    "assertArrayEquals",
    "assertSame",
    "assertNotSame",
    "failNotSame",
    "failNotEquals",
)

@IntellijInspection("KotlinMisorderedAssertEqualsArguments")
class KotlinMisorderedAssertEqualsArguments(config: Config) :
    Rule(
        config,
        "The `expected` argument of an assertion holds the tested value and the `actual` argument holds a constant. " +
            "Swap the arguments to get correct failure messages.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        val arguments = expression.valueArguments
        val isCandidate = callee.text in MISORDERED_ASSERT_METHOD_NAMES &&
            arguments.size >= 2 &&
            arguments.none { it.isNamed() }
        val methodName = if (isCandidate) analyze(expression) { expression.misorderedAssertMethodName() } else null
        if (methodName != null) report(Finding(Entity.from(callee), "Arguments to '$methodName()' are in wrong order"))
    }
}
