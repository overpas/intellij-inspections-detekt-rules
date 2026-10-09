package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private val CONVERT_TO_STRING_TEMPLATE_TOP_LEVEL_PLUS = setOf("kotlin.text.plus", "kotlin.plus")

internal class ConvertToStringTemplatePlus(private val session: KaSession) {

    fun isStringPlus(expression: KtBinaryExpression): Boolean =
        with(session) {
            val symbol = expression.operationReference.references
                .filterIsInstance<KtReference>()
                .firstNotNullOfOrNull { it.resolveToSymbol() } as? KaCallableSymbol
            val owner = symbol?.containingDeclaration as? KaNamedClassSymbol
            val isStringPlusSymbol = if (owner != null) {
                owner.classId == StandardClassIds.String
            } else {
                symbol?.callableId?.run { asSingleFqName().asString() } in CONVERT_TO_STRING_TEMPLATE_TOP_LEVEL_PLUS
            }
            expression.operationToken == KtTokens.PLUS &&
                expression.expressionType?.isStringType == true &&
                isStringPlusSymbol
        }

    fun isToString(expression: KtDotQualifiedExpression): Boolean =
        with(session) {
            val call = expression.selectorExpression as? KtCallExpression
            val callee = call?.calleeExpression as? KtNameReferenceExpression
            call?.run { valueArguments.isEmpty() } == true &&
                callee?.getReferencedName() == "toString" &&
                callee.references.filterIsInstance<KtReference>().flatMap { it.resolveToSymbols() }.any {
                    it is KaNamedFunctionSymbol && it.valueParameters.isEmpty() && it.returnType.isStringType
                }
        }
}
