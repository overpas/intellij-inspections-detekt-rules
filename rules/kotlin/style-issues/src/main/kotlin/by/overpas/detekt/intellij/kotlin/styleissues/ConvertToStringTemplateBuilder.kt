package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

internal class ConvertToStringTemplateBuilder(
    private val session: KaSession,
    private val plus: ConvertToStringTemplatePlus,
) {

    fun hasOnlySimpleEntries(expression: KtBinaryExpression): Boolean {
        val operand = ConvertToStringTemplateOperand(session, plus, expression.right)
        val text = expression.left.fold(operand.text(isBraceForced = false, nextText = null))
        val template = KtPsiFactory(expression.project).createExpression(text) as? KtStringTemplateExpression
        val entries = template?.entries.orEmpty()
        return entries.none { it is KtBlockStringTemplateEntry } &&
            entries.any { it !is KtLiteralStringTemplateEntry && it !is KtEscapeStringTemplateEntry } &&
            entries.any { it is KtLiteralStringTemplateEntry }
    }

    private tailrec fun KtExpression?.fold(right: String): String {
        val first = right.firstOrNull()
        val isBraceForced = first != null &&
            first != '$' &&
            (first.isJavaIdentifierPart() || first.isLetter() || first == '_' || first == '{' || first == '`')
        return if (this is KtBinaryExpression && plus.isStringPlus(this)) {
            val operand = ConvertToStringTemplateOperand(session, plus, this.right)
            left.fold(operand.text(isBraceForced, right) + right)
        } else {
            "\"" + ConvertToStringTemplateOperand(session, plus, this).text(isBraceForced, right) + right + "\""
        }
    }
}
