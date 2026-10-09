package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private val alwaysSetConvertibleTypes = setOf(
    StandardClassIds.Array,
    StandardClassIds.Iterable,
    StandardClassIds.MutableIterable,
    StandardClassIds.Collection,
    StandardClassIds.MutableCollection,
    StandardClassIds.List,
    StandardClassIds.MutableList,
    ClassId.fromString("kotlin/collections/AbstractMutableList"),
    ClassId.fromString("kotlin/collections/ArrayList"),
    ClassId.fromString("java/util/ArrayList"),
)

private val sequenceClassId = ClassId.fromString("kotlin/sequences/Sequence")

context(session: KaSession)
internal fun KtExpression.isConvertibleToSet(): Boolean {
    val type = with(session) { expressionType?.takeUnless { it.isMarkedNullable } }
    val isAlwaysConvertible = type?.symbol?.classId in alwaysSetConvertibleTypes
    return when {
        type == null || isConstantCollectionForSet() -> false
        isAlwaysConvertible -> !isSetHiddenByElvis()
        else -> with(session) { type.isSubtypeOf(sequenceClassId) }
    }
}

context(session: KaSession)
private fun KtExpression.isSetHiddenByElvis(): Boolean {
    val elvis = (KtPsiUtil.safeDeparenthesize(this) as? KtBinaryExpression)
        ?.takeIf { it.operationToken == KtTokens.ELVIS }
    val operands = elvis?.run { listOf(left, right) }.orEmpty()
    val operandClassIds = with(session) { operands.map { it?.expressionType?.symbol?.classId } }
    return elvis != null &&
        operands.zip(operandClassIds).all { (operand, classId) ->
            operand == null || classId == null || classId == StandardClassIds.Set || operand.isSetHiddenByElvis()
        }
}
