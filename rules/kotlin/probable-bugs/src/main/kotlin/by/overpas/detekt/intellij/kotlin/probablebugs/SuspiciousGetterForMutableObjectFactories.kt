package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression

private val SUSPICIOUS_GETTER_COROUTINE_FACTORIES = mapOf(
    "kotlinx.coroutines" to listOf(
        "Job",
        "SupervisorJob",
        "CompletableDeferred",
        "CoroutineScope",
        "MainScope",
    ),
    "kotlinx.coroutines.flow" to listOf("MutableStateFlow", "MutableSharedFlow"),
    "kotlinx.coroutines.channels" to listOf("Channel"),
    "kotlinx.coroutines.sync" to listOf("Mutex", "Semaphore"),
).flatMap { (packageName, names) ->
    names.map { CallableId(FqName(packageName), Name.identifier(it)) }
}

private val SUSPICIOUS_GETTER_COLLECTION_FACTORIES = listOf(
    "mutableListOf",
    "arrayListOf",
    "mutableSetOf",
    "hashSetOf",
    "linkedSetOf",
    "sortedSetOf",
    "mutableMapOf",
    "hashMapOf",
    "linkedMapOf",
    "sortedMapOf",
).map { CallableId(FqName("kotlin.collections"), Name.identifier(it)) }

internal fun KtPropertyAccessor.suspiciousGetterFactoryCall(): KtCallExpression? =
    takeIf { isGetter }
        ?.run { bodyBlockExpression?.singleReturnedExpression() ?: bodyExpression }
        as? KtCallExpression

private fun KtBlockExpression.singleReturnedExpression(): KtExpression? =
    (statements.singleOrNull() as? KtReturnExpression)?.returnedExpression

context(session: KaSession)
internal fun KtCallExpression.suspiciousGetterMessage(property: KtProperty): String? {
    val call = with(session) { resolveToCall()?.successfulFunctionCallOrNull() } ?: return null
    val callableId = call.symbol.callableId
    val coroutineFactory = SUSPICIOUS_GETTER_COROUTINE_FACTORIES.firstOrNull { it == callableId }
    val isCollection = callableId in SUSPICIOUS_GETTER_COLLECTION_FACTORIES &&
        call.combinedArgumentMapping.isEmpty() &&
        !property.hasModifier(KtTokens.OVERRIDE_KEYWORD)
    val collectionMessage =
        "Getter returns a new mutable collection on each access".takeIf { isCollection }
    return coroutineFactory
        ?.let { "Getter returns a new '${it.callableName}' on each access" }
        ?: collectionMessage
}
