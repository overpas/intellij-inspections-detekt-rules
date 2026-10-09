package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getOutermostParenthesizerOrThis
import org.jetbrains.kotlin.psi.psiUtil.getParentOfTypes

class ReplaceNotNullAssertionWithElvisReturn(config: Config) :
    Rule(
        config,
        "A not-null assertion in a function that returns `Unit` or a nullable type throws where an early return " +
            "would do. Replace `!!` with `?: return`.",
    ),
    RequiresAnalysisApi {

    @OptIn(KaExperimentalApi::class)
    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        val operation = expression.operationReference
        if (expression.baseExpression == null || operation.getReferencedNameElementType() != KtTokens.EXCLEXCL) return
        if (expression.getOutermostParenthesizerOrThis().parent is KtReturnExpression) return
        val owner = expression.getParentOfTypes(true, KtLambdaExpression::class.java, KtNamedFunction::class.java)
        val isReplaceable = owner != null && owner.hasElvisReturnLabel() && analyze(expression) {
            expression.directDiagnostics(KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS)
                .none { it is KaFirDiagnostic.UnnecessaryNotNullAssertion } &&
                owner.allowsElvisReturn()
        }
        if (isReplaceable) report(Finding(Entity.from(operation), "Replace '!!' with '?: return'"))
    }
}
