package by.overpas.detekt.intellij.kotlin.styleissues.replaceassertbooleanwithassertequality

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private val booleanOrNullLiterals = setOf(KtNodeTypes.BOOLEAN_CONSTANT, KtNodeTypes.NULL)

internal fun KtBinaryExpression.hasBooleanOrNullLiteralOperand(): Boolean =
    listOf(left, right).map(KtPsiUtil::deparenthesize).any { operand ->
        operand is KtConstantExpression && operand.node.elementType in booleanOrNullLiterals
    }
