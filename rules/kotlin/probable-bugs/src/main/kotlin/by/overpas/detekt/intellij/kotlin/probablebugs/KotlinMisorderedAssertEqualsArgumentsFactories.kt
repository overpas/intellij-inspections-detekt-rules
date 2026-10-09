package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol

private val EXPECTED_LIKE_FACTORY_CALLS = setOf(
    "kotlin.arrayOf",
    "kotlin.booleanArrayOf",
    "kotlin.byteArrayOf",
    "kotlin.charArrayOf",
    "kotlin.collections.listOf",
    "kotlin.collections.mapOf",
    "kotlin.collections.setOf",
    "kotlin.doubleArrayOf",
    "kotlin.emptyArray",
    "kotlin.floatArrayOf",
    "kotlin.intArrayOf",
    "kotlin.longArrayOf",
    "kotlin.shortArrayOf",
)

context(session: KaSession)
internal fun KaFunctionSymbol.isExpectedLikeFactory(): Boolean {
    val constructedClass = (this as? KaConstructorSymbol)?.run { returnType.symbol }
    return (constructedClass as? KaNamedClassSymbol)?.isData == true ||
        callableId?.run { asSingleFqName().asString() } in EXPECTED_LIKE_FACTORY_CALLS
}
