package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter

internal fun KtClass.arrayInDataClassKind(): String? =
    when {
        isData() -> "data class"
        isValue() -> "value class"
        else -> null
    }

context(session: KaSession)
internal fun KtClass.arrayInDataClassParameters(): List<KtParameter> =
    with(session) {
        val functions = declarations.asSequence()
            .filterIsInstance<KtNamedFunction>()
            .map { it.symbol }
            .filterIsInstance<KaNamedFunctionSymbol>()
            .filter { it.modality != KaSymbolModality.ABSTRACT && it.typeParameters.isEmpty() }
            .toList()
        val isEqualsOverridden = functions.any { function ->
            function.name.asString() == "equals" &&
                function.returnType.isBooleanType &&
                function.valueParameters.singleOrNull()
                    ?.run { returnType.isAnyType && returnType.isMarkedNullable } == true
        }
        val isHashCodeOverridden = functions.any { function ->
            function.name.asString() == "hashCode" &&
                function.valueParameters.isEmpty() &&
                function.returnType.isIntType
        }
        primaryConstructorParameters
            .filter { it.hasValOrVar() && it.symbol.returnType.isArrayOrPrimitiveArray }
            .takeUnless { isEqualsOverridden && isHashCodeOverridden }
            .orEmpty()
    }
