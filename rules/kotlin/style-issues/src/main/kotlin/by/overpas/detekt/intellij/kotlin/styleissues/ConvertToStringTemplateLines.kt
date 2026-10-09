package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression

internal object ConvertToStringTemplateLines {

    fun isSingleLine(expression: KtExpression): Boolean =
        generateSequence(listOf<KtExpression?>(expression)) { level ->
            level.asSequence()
                .filterIsInstance<KtBinaryExpression>()
                .filter { it.operationToken == KtTokens.PLUS }
                .flatMap { listOf(it.left, it.right) }
                .toList()
                .takeIf { it.isNotEmpty() }
        }
            .flatten()
            .filterNot { it is KtBinaryExpression && it.operationToken == KtTokens.PLUS }
            .all { it != null && !PsiUtilCore.hasErrorElementChild(it) && !it.textContains('\n') }
}
