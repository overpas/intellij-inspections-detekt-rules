package by.overpas.detekt.intellij.kotlin.logging.kotlinloggerinitializedwithforeignclass

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression

internal val KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_FACTORIES = listOf(
    "java.util.logging.Logger.getLogger",
    "org.slf4j.LoggerFactory.getLogger",
    "org.apache.commons.logging.LogFactory.getLog",
    "org.apache.log4j.Logger.getLogger",
    "org.apache.logging.log4j.LogManager.getLogger",
)

internal class KotlinLoggerInitializedWithForeignClassFactories(private val fqNames: Set<String>) {

    val methodNames = fqNames.map { it.substringAfterLast('.') }.toSet()

    context(session: KaSession)
    fun isFactoryCall(call: KtCallExpression): Boolean =
        with(session) { call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } }
            ?.run { asSingleFqName().asString() } in fqNames
}
