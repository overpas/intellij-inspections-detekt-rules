package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaJavaFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.analysis.api.symbols.KaSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

internal class UsePropertyAccessSyntaxTarget(
    private val accessor: UsePropertyAccessSyntaxAccessor,
    private val symbol: KaFunctionSymbol,
    private val receiverType: KaType,
) {

    @OptIn(KaExperimentalApi::class)
    context(session: KaSession)
    fun convertibleProperty(): KaSyntheticJavaPropertySymbol? =
        with(session) {
            val property = receiverType.syntheticJavaPropertiesScope?.run {
                getCallableSignatures { !it.isSpecial }
                    .map { it.symbol }
                    .filterIsInstance<KaSyntheticJavaPropertySymbol>()
                    .firstOrNull { it.name.asString() in accessor.propertyNames && it.name.asString() !in keywords }
            } ?: return null
            val overridden = (symbol.allOverriddenSymbols + symbol).toList()
            val isFromJava = overridden.none { it.callableId?.run { asSingleFqName().asString() } in notProperties } &&
                overridden.any { overriddenSymbol ->
                    overriddenSymbol.origin in javaOrigins && overriddenSymbol.directlyOverriddenSymbols.none()
                }
            val typeScope = receiverType.scope
            val hasVisibleField = typeScope != null &&
                typeScope.declarationScope.callables(property.name)
                    .any { it is KaJavaFieldSymbol && it.visibility != KaSymbolVisibility.PRIVATE }
            val isTrivial = symbol.psi?.isTrivialAccessor() == true
            property.takeIf { isFromJava && !hasVisibleField && isTrivial && accessor.matches(property, symbol) }
        }

    private companion object {

        val keywords = KtTokens.KEYWORDS.types.map { it.toString() }.toSet()

        val javaOrigins = setOf(KaSymbolOrigin.JAVA_SOURCE, KaSymbolOrigin.JAVA_LIBRARY)

        val notProperties = buildList {
            listOf("java.net.Socket", "java.net.URLConnection")
                .flatMapTo(this) { owner -> listOf("getInputStream", "getOutputStream").map { "$owner.$it" } }
            listOf("AtomicInteger", "AtomicLong").flatMapTo(this) { owner ->
                listOf("getAndIncrement", "getAndDecrement", "getAcquire", "getOpaque", "getPlain")
                    .map { method -> "java.util.concurrent.atomic.$owner.$method" }
            }
            listOf("getChar", "getDouble", "getFloat", "getInt", "getLong", "getShort")
                .mapTo(this) { "java.nio.ByteBuffer.$it" }
        }

        fun PsiElement.isTrivialAccessor(): Boolean =
            when (this) {
                is KtNamedFunction -> bodyBlockExpression?.statements.orEmpty().size == 1
                is PsiCompiledElement -> true
                is PsiMethod -> body?.statements.orEmpty().size == 1
                else -> false
            }
    }
}
