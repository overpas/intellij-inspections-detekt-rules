package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtSimpleNameStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getContentRange
import org.jetbrains.kotlin.psi.psiUtil.isSingleQuoted

internal class ConvertToStringTemplateLiteral(
    private val template: KtStringTemplateExpression,
    private val isBraceForced: Boolean,
    private val nextText: String?,
) {

    private val withoutPrefix: KtStringTemplateExpression
        get() {
            val quote = if (template.isSingleQuoted()) "\"" else "\"\"\""
            val dollar = if (template.isSingleQuoted()) "\\$" else $$"${'$'}"
            val content = template.entries.joinToString(separator = "") { entry ->
                when (entry) {
                    is KtLiteralStringTemplateEntry -> entry.text.replace("$", dollar)
                    is KtSimpleNameStringTemplateEntry -> "$" + entry.expression?.text.orEmpty()
                    is KtBlockStringTemplateEntry -> $$"${" + entry.expression?.text.orEmpty() + "}"
                    else -> entry.text
                }
            }
            val factory = KtPsiFactory(template.project)
            return template.takeIf { it.interpolationPrefix == null }
                ?: factory.createExpression(quote + content + quote) as KtStringTemplateExpression
        }

    val text: String
        get() {
            val plain = withoutPrefix
            val raw = plain.getContentRange().substring(plain.text)
            val content = if (plain.isSingleQuoted()) raw else StringUtil.escapeStringCharacters(raw)
            val isTailDollarUnescaped = content.endsWith('$') && !content.endsWith("\\$")
            val last = plain.entries.lastOrNull()
            return when {
                !isBraceForced && !(isTailDollarUnescaped && nextText?.startsWith('{') == true) -> content

                isTailDollarUnescaped -> content.dropLast(1) + "\\$"

                last is KtSimpleNameStringTemplateEntry ->
                    content.dropLast(last.textLength) + $$"${" + last.text.drop(1) + "}"

                else -> content
            }
        }
}
