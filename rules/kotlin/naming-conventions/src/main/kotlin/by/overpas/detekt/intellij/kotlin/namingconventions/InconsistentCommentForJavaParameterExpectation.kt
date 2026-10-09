package by.overpas.detekt.intellij.kotlin.namingconventions

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtValueArgument

internal class InconsistentCommentForJavaParameterExpectation(parameter: KaValueParameterSymbol) {

    private val commentedName = if (parameter.isVararg) "...${parameter.name}" else parameter.name.asString()

    val parameterName = parameter.name.asString()

    fun isMetBy(comment: PsiComment): Boolean =
        comment.text
            .removePrefix("/*")
            .removeSuffix("*/")
            .trim()
            .removeSuffix("=")
            .trim() == commentedName
}

internal fun KtValueArgument.parameterNameComment(): PsiComment? =
    siblings(forward = false, withSelf = false)
        .takeWhile { it is PsiWhiteSpace || it is PsiComment }
        .filterIsInstance<PsiComment>()
        .firstOrNull { it.elementType == KtTokens.BLOCK_COMMENT && it.text.removeSuffix("*/").trim().endsWith("=") }
