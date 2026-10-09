package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtLabelReferenceExpression
import org.jetbrains.kotlin.psi.KtOperationReferenceExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtStatementExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtValueArgumentName
import org.jetbrains.kotlin.psi.psiUtil.parents

internal fun KtExpression.isUnusedFlowCandidate(): Boolean =
    this !is KtOperationReferenceExpression &&
        this !is KtLabelReferenceExpression &&
        (this as? KtBinaryExpression)?.operationToken != KtTokens.EQ &&
        parents.none { it is KtValueArgumentName || it is KtImportDirective || it is KtPackageDirective } &&
        parents.takeWhile { it is KtExpression && it !is KtBlockExpression }.none { it is KtReturnExpression } &&
        parents.takeWhile { it !is KtBlockExpression && it !is KtStatementExpression }
            .none { it is KtDotQualifiedExpression || it is KtCallExpression || it is KtThisExpression }
