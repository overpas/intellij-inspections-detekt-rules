package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaJavaFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaLocalVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaNamedSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

context(session: KaSession)
internal fun KtNameReferenceExpression.isExpectedLikeAssertReference(isActual: Boolean): Boolean {
    val symbol = references
        .filterIsInstance<KtReference>()
        .firstNotNullOfOrNull { with(session) { it.resolveToSymbol() } }
    val isNamedExpected = (symbol as? KaNamedSymbol)?.run { name.asString() } == "expected"
    return when (symbol) {
        is KaEnumEntrySymbol, is KaClassSymbol -> true

        is KaNamedFunctionSymbol -> isActual && isNamedExpected

        is KaValueParameterSymbol -> isNamedExpected

        is KaLocalVariableSymbol, is KaPropertySymbol ->
            isNamedExpected || symbol.hasExpectedAssertInitializer(isActual)

        is KaJavaFieldSymbol -> symbol.isStatic && symbol.isVal

        else -> false
    }
}

context(session: KaSession)
private fun KaVariableSymbol.hasExpectedAssertInitializer(isActual: Boolean): Boolean {
    val property = psi as? KtProperty
    val isStable = property != null &&
        ((property.isLocal && !property.isVar) || property.hasModifier(KtTokens.CONST_KEYWORD))
    return isStable && property.initializer?.isExpectedLikeAssertArgument(isActual) == true
}
