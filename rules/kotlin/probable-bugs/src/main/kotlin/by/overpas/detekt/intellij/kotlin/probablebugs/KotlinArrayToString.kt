package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression

private const val KOTLIN_ARRAY_TO_STRING_IMPLICIT_MESSAGE = "Implicit 'toString()' called on array"

class KotlinArrayToString(config: Config) :
    Rule(
        config,
        "`toString()` of an array returns its type and identity hash, not its contents. " +
            "Use `contentToString()` or `contentDeepToString()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val call = expression.noArgumentCallNamed(KOTLIN_ARRAY_TO_STRING_CALLABLE_ID.callableName) ?: return
        val isArrayToString = analyze(expression) { expression.isArrayMemberCall(KOTLIN_ARRAY_TO_STRING_CALLABLE_ID) }
        if (isArrayToString) report(Finding(Entity.from(call), "'toString()' called on array"))
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val operation =
            expression.operationReference.takeIf { it.getReferencedNameElementType() == KtTokens.PLUS } ?: return
        val isArrayConcatenation = analyze(expression) { expression.isStringPlusArray() }
        if (isArrayConcatenation) report(Finding(Entity.from(operation), KOTLIN_ARRAY_TO_STRING_IMPLICIT_MESSAGE))
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.implicitToStringCalleeOrNull() ?: return
        val isArrayArgument = analyze(expression) { expression.hasImplicitArrayToString() }
        if (isArrayArgument) report(Finding(Entity.from(callee), KOTLIN_ARRAY_TO_STRING_IMPLICIT_MESSAGE))
    }

    override fun visitStringTemplateEntryWithExpression(entry: KtStringTemplateEntryWithExpression) {
        super.visitStringTemplateEntryWithExpression(entry)
        val expression = entry.expression ?: return
        val isArray = analyze(expression) { expression.expressionType?.isArrayOrPrimitiveArray == true }
        if (isArray) report(Finding(Entity.from(expression), KOTLIN_ARRAY_TO_STRING_IMPLICIT_MESSAGE))
    }
}
