package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression

class SuspiciousCollectionReassignment(config: Config) :
    Rule(
        config,
        "An augmented assignment on a 'var' of a read-only collection creates a new collection each time. " +
            "Use a mutable collection with 'val', or use a plain assignment.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val operation = expression.operationReference
        val isAugmented = expression.operationToken == KtTokens.PLUSEQ || expression.operationToken == KtTokens.MINUSEQ
        if (!isAugmented || expression.right == null) return
        val typeName = SuspiciousCollectionReassignmentTarget(expression).readOnlyTypeName() ?: return
        report(
            Finding(
                Entity.from(operation),
                "'${operation.text}' on a read-only $typeName creates a new $typeName under the hood",
            ),
        )
    }
}
