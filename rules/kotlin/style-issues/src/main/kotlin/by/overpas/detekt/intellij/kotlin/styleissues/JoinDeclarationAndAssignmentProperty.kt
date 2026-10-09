package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.siblings

context(session: KaSession)
internal fun KtProperty.hasJoinableTypeWith(initializer: KtExpression): Boolean =
    with(session) {
        val initializerType = initializer.expressionType
        val propertyType = typeReference?.type
        val isNonLocalVar = isVar && !isLocal
        val isSubtype = initializerType != null &&
            propertyType != null &&
            (initializerType.semanticallyEquals(propertyType) || initializerType.isSubtypeOf(propertyType))
        isNonLocalVar || isSubtype
    }

context(session: KaSession)
internal fun KtProperty.isLateinitUsedBefore(assignment: KtBinaryExpression): Boolean =
    with(session) {
        val propertySymbol = symbol
        isLocal &&
            hasModifier(KtTokens.LATEINIT_KEYWORD) &&
            siblings(forward = true, withItself = false)
                .takeWhile { it != assignment }
                .any { propertySymbol in it.joinReferencedSymbols() }
    }
