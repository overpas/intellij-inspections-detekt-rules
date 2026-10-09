package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.diagnostics.KaSeverity
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class UnlabeledReturnInsideLambda(config: Config) :
    Rule(
        config,
        "An unlabeled `return` inside an inline lambda returns from the enclosing function. " +
            "Add a label that names the enclosing function.",
    ),
    RequiresAnalysisApi {

    override fun visitReturnExpression(expression: KtReturnExpression) {
        super.visitReturnExpression(expression)
        val isUnlabeledInsideLambda = expression.labelQualifier == null &&
            expression.getParentOfType<KtLambdaExpression>(true, KtNamedFunction::class.java) != null &&
            expression.getStrictParentOfType<KtNamedFunction>()?.nameIdentifier != null
        if (isUnlabeledInsideLambda && !expression.hasError()) {
            report(Finding(Entity.from(expression.returnKeyword), "Unlabeled return inside lambda"))
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KtReturnExpression.hasError(): Boolean =
        analyze(this) {
            directDiagnostics(KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS).any { it.severity == KaSeverity.ERROR }
        }
}
