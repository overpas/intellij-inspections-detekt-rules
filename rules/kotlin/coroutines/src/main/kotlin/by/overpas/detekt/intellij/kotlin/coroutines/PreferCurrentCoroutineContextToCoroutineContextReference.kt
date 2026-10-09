package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private val COROUTINE_CONTEXT_ID = CallableId(FqName("kotlin.coroutines"), Name.identifier("coroutineContext"))

private val CURRENT_COROUTINE_CONTEXT_ID = CallableId(
    FqName("kotlinx.coroutines"),
    Name.identifier("currentCoroutineContext"),
)

internal class PreferCurrentCoroutineContextToCoroutineContextReference(
    private val expression: KtNameReferenceExpression,
) {

    fun isAmbiguous(): Boolean =
        expression.getReferencedNameAsName() == COROUTINE_CONTEXT_ID.callableName &&
            analyze(expression) {
                val symbol = expression.references
                    .filterIsInstance<KtReference>()
                    .firstNotNullOfOrNull { it.resolveToSymbol() }
                (symbol as? KaPropertySymbol)?.callableId == COROUTINE_CONTEXT_ID &&
                    findTopLevelCallables(
                        CURRENT_COROUTINE_CONTEXT_ID.packageName,
                        CURRENT_COROUTINE_CONTEXT_ID.callableName,
                    ).any()
            }
}
