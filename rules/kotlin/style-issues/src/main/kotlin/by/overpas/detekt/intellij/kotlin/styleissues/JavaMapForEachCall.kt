package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal fun KtCallExpression.hasJavaMapForEachShape(): Boolean {
    val parameters = valueArguments
        .singleOrNull()
        ?.getArgumentExpression()
        ?.let { KtPsiUtil.safeDeparenthesize(it) as? KtLambdaExpression }
        ?.valueParameters
        .orEmpty()
    return calleeExpression?.text == "forEach" &&
        parameters.size == 2 &&
        parameters.all { it.destructuringDeclaration == null }
}

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
internal fun KtCallExpression.isJavaMapForEachCall(): Boolean =
    with(session) {
        val call = resolveCall() as? KaSingleCall<*, *>
        call?.dispatchReceiver?.run { type.isSubtypeOf(StandardClassIds.Map) } == true
    }
