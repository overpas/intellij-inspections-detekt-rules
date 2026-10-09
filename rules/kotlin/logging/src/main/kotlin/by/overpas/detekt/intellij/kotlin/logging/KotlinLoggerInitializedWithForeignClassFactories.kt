package by.overpas.detekt.intellij.kotlin.logging

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression

private val KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_FACTORIES = setOf(
    FqName("java.util.logging.Logger.getLogger"),
    FqName("org.slf4j.LoggerFactory.getLogger"),
    FqName("org.apache.commons.logging.LogFactory.getLog"),
    FqName("org.apache.log4j.Logger.getLogger"),
    FqName("org.apache.logging.log4j.LogManager.getLogger"),
)

internal val KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_METHOD_NAMES =
    KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_FACTORIES.map { it.shortName().asString() }.toSet()

context(session: KaSession)
internal fun KtCallExpression.isLoggerFactoryCall(): Boolean =
    with(session) { resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } }
        ?.asSingleFqName() in KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_FACTORIES
