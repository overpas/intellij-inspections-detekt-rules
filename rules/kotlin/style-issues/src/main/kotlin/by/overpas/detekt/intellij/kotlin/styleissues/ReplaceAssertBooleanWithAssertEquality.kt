package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private val replaceAssertBooleanWithAssertEqualityPackage = FqName("kotlin.test")

private val replaceAssertBooleanWithAssertEqualityNames = setOf("assertTrue", "assertFalse")

private val replaceAssertBooleanWithAssertEqualityTokens = setOf(KtTokens.EQEQ, KtTokens.EQEQEQ)

class ReplaceAssertBooleanWithAssertEquality(config: Config) :
    Rule(
        config,
        "`assertTrue` or `assertFalse` with an equality check gives a less clear failure message. " +
            "Use `assertEquals`, `assertNotEquals`, `assertSame` or `assertNotSame` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
        val condition = expression.valueArguments.firstOrNull()?.getArgumentExpression() as? KtBinaryExpression
        val isCandidate = callee.getReferencedName() in replaceAssertBooleanWithAssertEqualityNames &&
            expression.valueArguments.size in 1..2 &&
            condition?.operationToken in replaceAssertBooleanWithAssertEqualityTokens
        if (isCandidate && condition?.isKotlinTestAssertionArgument(expression) == true) {
            report(Finding(Entity.from(callee), "Replace assert boolean with assert equality"))
        }
    }

    private fun KtBinaryExpression.isKotlinTestAssertionArgument(assertion: KtCallExpression): Boolean =
        analyze(this) {
            val packageName = assertion.resolveToCall()
                ?.successfulFunctionCallOrNull()
                ?.run { symbol.callableId?.packageName }
            val leftType = left?.expressionType
            val rightType = right?.expressionType
            packageName == replaceAssertBooleanWithAssertEqualityPackage &&
                leftType != null &&
                rightType != null &&
                (leftType.isSubtypeOf(rightType) || rightType.isSubtypeOf(leftType))
        }
}
