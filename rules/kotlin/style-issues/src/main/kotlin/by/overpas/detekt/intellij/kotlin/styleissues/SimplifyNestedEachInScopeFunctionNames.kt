package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression

private val simplifyNestedEachFqNames: Map<String, Set<FqName>> =
    mapOf(
        SIMPLIFY_NESTED_EACH_ALSO to setOf(FqName("kotlin.also")),
        SIMPLIFY_NESTED_EACH_APPLY to setOf(FqName("kotlin.apply")),
        SIMPLIFY_NESTED_EACH_FOR_EACH to setOf(FqName("kotlin.collections.forEach"), FqName("kotlin.text.forEach")),
        SIMPLIFY_NESTED_EACH_ON_EACH to setOf(FqName("kotlin.collections.onEach"), FqName("kotlin.text.onEach")),
    )

context(session: KaSession)
internal fun KtCallExpression.resolvedNestedEachName(): String? =
    with(session) {
        val name = calleeExpression?.text
        val fqName = resolveToCall()?.successfulFunctionCallOrNull()?.symbol?.callableId?.asSingleFqName()
        name?.takeIf { fqName in simplifyNestedEachFqNames[it].orEmpty() }
    }
