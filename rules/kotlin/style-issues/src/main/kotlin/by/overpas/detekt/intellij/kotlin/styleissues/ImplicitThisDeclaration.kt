package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.receiverType
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType

context(session: KaSession)
internal fun KaSymbol.implicitThisClass(): KaClassSymbol? =
    with(session) {
        when (val symbol = this@implicitThisClass) {
            is KaNamedFunctionSymbol, is KaPropertySymbol ->
                if (symbol.isExtension) {
                    symbol.receiverType?.expandedSymbol
                } else {
                    symbol.containingDeclaration as? KaClassSymbol
                }

            is KaVariableSymbol -> (symbol.returnType as? KaFunctionType)?.receiverType?.expandedSymbol

            else -> null
        }
    }
