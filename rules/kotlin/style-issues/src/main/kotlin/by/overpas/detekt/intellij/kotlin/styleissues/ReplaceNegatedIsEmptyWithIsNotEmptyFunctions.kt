package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private val REPLACE_NEGATED_IS_EMPTY_OWNERS = listOf(
    "java.util.ArrayList",
    "java.util.HashMap",
    "java.util.HashSet",
    "java.util.LinkedHashMap",
    "java.util.LinkedHashSet",
    "kotlin.collections",
    "kotlin.collections.List",
    "kotlin.collections.Set",
    "kotlin.collections.Map",
    "kotlin.collections.MutableList",
    "kotlin.collections.MutableSet",
    "kotlin.collections.MutableMap",
    "kotlin.text",
)

internal val REPLACE_NEGATED_IS_EMPTY_FUNCTIONS: Map<String, Pair<List<FqName>, String>> = mapOf(
    "isEmpty" to (REPLACE_NEGATED_IS_EMPTY_OWNERS.map { FqName("$it.isEmpty") } to "isNotEmpty"),
    "isNotEmpty" to (REPLACE_NEGATED_IS_EMPTY_OWNERS.map { FqName("$it.isNotEmpty") } to "isEmpty"),
    "isBlank" to (listOf(FqName("kotlin.text.isBlank")) to "isNotBlank"),
    "isNotBlank" to (listOf(FqName("kotlin.text.isNotBlank")) to "isBlank"),
)

internal fun KtPrefixExpression.negatedEmptinessCall(): KtCallExpression? {
    val base = KtPsiUtil.deparenthesize(baseExpression)
    val call = (base as? KtQualifiedExpression)?.selectorExpression ?: base
    return (call as? KtCallExpression)?.takeIf { operationToken == KtTokens.EXCL }
}
