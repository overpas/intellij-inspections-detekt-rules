package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

class SuspiciousJavaClassCallableReference(config: Config) :
    Rule(
        config,
        "`::javaClass` references the `javaClass` property instead of reading the Java class. " +
            "Use `.javaClass` on an expression or `::class.java` on a type.",
    ),
    RequiresAnalysisApi {

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        val reference = expression.callableReference.takeIf { it.hasJavaClassName() } ?: return
        if (analyze(reference) { reference.isJavaClassReference() }) {
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    private companion object {
        const val MESSAGE =
            "'::javaClass' is often used by mistake instead of '.javaClass' or '::class.java'"
    }
}
