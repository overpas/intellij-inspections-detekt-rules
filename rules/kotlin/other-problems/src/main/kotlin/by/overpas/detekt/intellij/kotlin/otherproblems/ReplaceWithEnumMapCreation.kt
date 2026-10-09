package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtCallExpression

private val replaceWithEnumMapCreationFqNames = setOf(
    "java.util.HashMap",
    "kotlin.collections.HashMap",
    "kotlin.collections.hashMapOf",
)

private val replaceWithEnumMapCreationShortNames = setOf("HashMap", "hashMapOf")

internal fun KtCallExpression.mayCreateHashMap(): Boolean {
    val calleeText = calleeExpression?.text
    return calleeText in replaceWithEnumMapCreationFqNames ||
        calleeText in replaceWithEnumMapCreationShortNames ||
        containingKtFile.importDirectives.any {
            it.importedFqName?.asString() in replaceWithEnumMapCreationFqNames && it.aliasName == calleeText
        }
}

context(session: KaSession)
internal fun KtCallExpression.createsHashMapWithEnumKeys(): Boolean =
    with(session) {
        val fqName = when (val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol) {
            is KaConstructorSymbol -> symbol.containingClassId?.asFqNameString()
            is KaNamedFunctionSymbol -> symbol.callableId?.run { asSingleFqName().asString() }
            else -> null
        }
        val keyType = (expectedType as? KaClassType)?.run { typeArguments.firstOrNull()?.type }
        fqName in replaceWithEnumMapCreationFqNames && keyType?.expandedSymbol?.classKind == KaClassKind.ENUM_CLASS
    }
