package by.overpas.detekt.intellij.kotlin.javainterop

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.classSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry

private val javaOrigins = setOf(KaSymbolOrigin.JAVA_SOURCE, KaSymbolOrigin.JAVA_LIBRARY)

context(session: KaSession)
internal fun KtDelegatedSuperTypeEntry.skipsDefaultMethodOverrides(declaration: KtClassOrObject): Boolean =
    with(session) {
        val delegatedInterface = typeReference?.run { type.symbol } as? KaNamedClassSymbol
        val interfaceMethods = delegatedInterface?.run { memberScope.callables.map { it.fakeOverrideOriginal }.toSet() }
        val inheritedDefaultMethods = declaration.classSymbol?.run { memberScope.callables }.orEmpty()
            .map { it.fakeOverrideOriginal }
            .filter { it.isJavaDefaultMethod() && it in interfaceMethods.orEmpty() }
            .toSet()
        val delegateClass = delegateExpression?.expressionType?.symbol as? KaClassSymbol
        val overrides = JavaDefaultMethodsNotOverriddenByDelegationOverrides(session, inheritedDefaultMethods)
        inheritedDefaultMethods.isNotEmpty() && with(overrides) { delegateClass?.overridesAnyMethod() } == true
    }

context(session: KaSession)
private fun KaCallableSymbol.isJavaDefaultMethod(): Boolean =
    this is KaNamedFunctionSymbol &&
        origin in javaOrigins &&
        modality != KaSymbolModality.ABSTRACT &&
        with(session) { (containingSymbol as? KaClassSymbol)?.classKind == KaClassKind.INTERFACE }
