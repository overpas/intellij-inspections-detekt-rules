package by.overpas.detekt.intellij.kotlin.logging

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression

private const val JAVA_CLASS_SELECTOR = "java"

private val KCLASS_NAME_SELECTORS = setOf(JAVA_CLASS_SELECTOR, "qualifiedName", "simpleName")

private val JAVA_CLASS_NAME_SELECTORS = setOf(
    "name",
    "simpleName",
    "canonicalName",
    "getName",
    "getSimpleName",
    "getCanonicalName",
)

internal fun KtCallExpression.foreignLoggerClassLiteral(): KtClassLiteralExpression? {
    val ownerNames = loggerOwnerNames()
    val classLiteral = calleeExpression
        ?.takeIf { it.text in KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_METHOD_NAMES }
        ?.let { valueArguments.singleOrNull()?.getArgumentExpression().loggerClassLiteral() }
    return classLiteral?.takeIf { ownerNames.isNotEmpty() && it.receiverExpression?.text !in ownerNames }
}

private fun KtExpression?.loggerClassLiteral(): KtClassLiteralExpression? {
    val argument = this as? KtDotQualifiedExpression
    val selector = argument?.selectorExpression
    val selectorName = (selector as? KtCallExpression)?.calleeExpression?.text ?: selector?.text
    return when (val receiver = argument?.receiverExpression) {
        is KtClassLiteralExpression -> receiver.takeIf { selectorName in KCLASS_NAME_SELECTORS }

        is KtDotQualifiedExpression -> (receiver.receiverExpression as? KtClassLiteralExpression)?.takeIf {
            receiver.selectorExpression?.text == JAVA_CLASS_SELECTOR && selectorName in JAVA_CLASS_NAME_SELECTORS
        }

        else -> null
    }
}
