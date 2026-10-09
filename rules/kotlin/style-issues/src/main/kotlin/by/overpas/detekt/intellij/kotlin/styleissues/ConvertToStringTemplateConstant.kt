package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.psi.KtConstantExpression

internal class ConvertToStringTemplateConstant(
    private val session: KaSession,
    private val constant: KtConstantExpression,
    private val isBraceForced: Boolean,
) {

    val text: String?
        get() {
            val value = with(session) { constant.evaluate() } ?: return $$"${$${constant.text}}"
            val isChar = value is KaConstantValue.CharValue
            val string = if (isChar) "${value.value}" else value.render()
            val additionalChars = if (isBraceForced) "\"$" else "\""
            return if (isChar || string == constant.text) {
                StringUtil.escapeStringCharacters(string.length, string, additionalChars, StringBuilder()).toString()
            } else {
                null
            }
        }
}
