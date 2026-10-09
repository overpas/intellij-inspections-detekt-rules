package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private val REPLACE_MAP_KEYS_ENTRY_KEY_FQ_NAME = FqName("kotlin.collections.Map.Entry.key")

context(session: KaSession)
internal fun KtLambdaExpression.mapsMapEntryToKey(): Boolean {
    val parameter = with(session) { functionLiteral.symbol.valueParameters.singleOrNull() }
    val statement = bodyExpression?.run { statements.singleOrNull() }
    val returned = KtPsiUtil.safeDeparenthesize(statement ?: return false) as? KtDotQualifiedExpression
    val selector = returned?.selectorExpression as? KtNameReferenceExpression
    val receiver = returned?.receiverExpression
    return parameter != null &&
        selector?.getReferencedName() == "key" &&
        receiver?.replaceMapKeysSymbol() == parameter &&
        selector.replaceMapKeysSymbol()?.callableId?.asSingleFqName() == REPLACE_MAP_KEYS_ENTRY_KEY_FQ_NAME
}

context(session: KaSession)
private fun KtExpression.replaceMapKeysSymbol(): KaVariableSymbol? =
    with(session) {
        references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() } as? KaVariableSymbol
    }
