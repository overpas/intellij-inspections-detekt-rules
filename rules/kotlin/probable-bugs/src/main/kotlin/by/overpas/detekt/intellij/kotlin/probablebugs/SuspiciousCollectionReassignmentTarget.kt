package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtProperty

internal class SuspiciousCollectionReassignmentTarget(private val expression: KtBinaryExpression) {

    private val readOnlyClassIds = setOf(StandardClassIds.List, StandardClassIds.Set, StandardClassIds.Map)

    fun readOnlyTypeName(): String? =
        analyze(expression) {
            val left = expression.left
            val symbol = left?.run {
                references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
            }
            val property = symbol?.psi as? KtProperty
            val classId = (left?.expressionType as? KaClassType)?.classId
            val isReported = classId != null &&
                property != null &&
                property.isVar &&
                classId in readOnlyClassIds &&
                ((property.isLocal && property.initializer != null) || isIterableRemoval(classId))
            classId?.takeIf { isReported }?.run { shortClassName.asString().lowercase() }
        }

    private fun KaSession.isIterableRemoval(classId: ClassId): Boolean {
        val iterable = findClass(StandardClassIds.Iterable)
        val rightClass = expression.right?.expressionType?.symbol as? KaNamedClassSymbol
        return expression.operationToken == KtTokens.MINUSEQ &&
            classId != StandardClassIds.Map &&
            iterable != null &&
            rightClass != null &&
            rightClass.isSubClassOf(iterable)
    }
}
