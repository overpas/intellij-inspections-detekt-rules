package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.psi.KtCallExpression

context(session: KaSession)
internal fun KtCallExpression.callsSoftDeprecatedEnumValues(): Boolean =
    with(session) {
        val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
        val containingClass = symbol?.containingDeclaration as? KaClassSymbol
        symbol != null &&
            containingClass?.classKind == KaClassKind.ENUM_CLASS &&
            symbol.callableId?.callableName == StandardNames.ENUM_VALUES &&
            symbol.valueParameters.isEmpty()
    }
