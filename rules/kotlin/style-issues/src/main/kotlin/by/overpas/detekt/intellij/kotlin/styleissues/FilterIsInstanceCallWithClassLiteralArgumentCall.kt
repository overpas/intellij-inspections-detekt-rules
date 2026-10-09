package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementSelector

private val FILTER_IS_INSTANCE_CALLABLE_ID =
    CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier("filterIsInstance"))

internal class FilterIsInstanceCallWithClassLiteralArgumentCall(private val call: KtCallExpression) {

    private val argument = call.valueArguments.singleOrNull()?.getArgumentExpression() as? KtDotQualifiedExpression

    private val classLiteral = argument?.receiverExpression as? KtClassLiteralExpression

    private val className = classLiteral?.receiverExpression?.getQualifiedElementSelector()

    fun isCandidate(): Boolean =
        call.calleeExpression?.text == "filterIsInstance" && classLiteral != null

    context(session: KaSession)
    fun isReplaceable(): Boolean =
        with(session) {
            val function = call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
            val klass = className?.run {
                references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
            } as? KaNamedClassSymbol
            function == FILTER_IS_INSTANCE_CALLABLE_ID && klass?.run { typeParameters.isEmpty() } == true
        }
}
