package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.signatures.KaFunctionSignature
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.load.java.propertyNamesByAccessorName
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

internal fun KaSession.isDataClassAnyMember(
    function: KtNamedFunction,
    superSymbol: KaCallableSymbol,
): Boolean =
    function.containingClassOrObject?.isData() == true &&
        (sequenceOf(superSymbol) + superSymbol.allOverriddenSymbols).any { it.isAnyMember() }

internal fun KaCallableSymbol.isAnyMember(): Boolean =
    callableId?.classId == StandardClassIds.Any

internal fun KaSession.parametersMatch(
    symbol: KaFunctionSymbol,
    superSignature: KaFunctionSignature<*>,
): Boolean {
    val parameters = symbol.valueParameters
    val superParameters = superSignature.valueParameters
    return parameters.size != superParameters.size ||
        parameters.zip(superParameters).all { (parameter, superParameter) ->
            parameter.returnType.semanticallyEquals(superParameter.returnType)
        }
}

internal fun KaSession.hasDerivedProperty(
    function: KtNamedFunction,
    symbol: KaFunctionSymbol,
): Boolean {
    val returnType = symbol.returnType
    val parameters = symbol.valueParameters
    val propertyType = when {
        returnType.isUnitType -> parameters.singleOrNull()?.returnType
        parameters.isEmpty() -> returnType
        else -> null
    }
    val propertyNames = function.nameAsName?.let { propertyNamesByAccessorName(it) }.orEmpty().map { it.asString() }
    val expectedType = propertyType?.withNullability(false)
    return expectedType != null &&
        function.containingClassOrObject?.declarations.orEmpty()
            .filterIsInstance<KtProperty>()
            .any {
                it.name in propertyNames && it.returnType.withNullability(false).semanticallyEquals(expectedType)
            }
}

internal fun KaCallableSymbol.isPackageVisibleJavaSymbol(): Boolean =
    origin.isJava() && visibility != KaSymbolVisibility.PUBLIC && visibility != KaSymbolVisibility.PRIVATE

private fun KaSymbolOrigin.isJava(): Boolean =
    this == KaSymbolOrigin.JAVA_SOURCE || this == KaSymbolOrigin.JAVA_LIBRARY

internal fun KaSession.isAmbiguouslyDerived(overridden: List<KaCallableSymbol>): Boolean =
    overridden.size > 1 &&
        overridden.any { symbol ->
            val kind = (symbol.containingDeclaration as? KaNamedClassSymbol)?.classKind
            val isAbstract = symbol.modality == KaSymbolModality.ABSTRACT
            symbol.origin.isJava() ||
                (kind == KaClassKind.CLASS && isAbstract) ||
                (kind == KaClassKind.INTERFACE && !isAbstract)
        }

internal fun KaSession.implementsDelegatedMember(
    function: KtNamedFunction,
    overridden: List<KaCallableSymbol>,
): Boolean {
    val delegatedTypes = function.containingClassOrObject?.superTypeListEntries.orEmpty()
        .mapNotNull { entry -> (entry as? KtDelegatedSuperTypeEntry)?.typeReference?.type }
    return overridden.any { symbol ->
        val containingType = (symbol.containingSymbol as? KaNamedClassSymbol)?.defaultType
        containingType != null && delegatedTypes.any { it.isSubtypeOf(containingType) }
    }
}
