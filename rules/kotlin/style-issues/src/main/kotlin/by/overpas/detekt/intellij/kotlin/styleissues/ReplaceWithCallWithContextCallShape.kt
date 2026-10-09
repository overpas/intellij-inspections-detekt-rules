package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal class ReplaceWithCallWithContextCallShape(call: KtCallExpression) {

    private val arguments = call.valueArguments

    private val receiverArgument = arguments.firstOrNull()?.takeIf {
        arguments.size == 2 && it !is KtLambdaArgument && !it.isNamed() && it.getSpreadElement() == null
    }

    private val isWithCall = call.parent !is KtQualifiedExpression &&
        (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() == "with" &&
        receiverArgument?.getArgumentExpression() != null

    val lambda = (
        call.lambdaArguments.singleOrNull()?.getLambdaExpression()
            ?: arguments.getOrNull(1)?.getArgumentExpression() as? KtLambdaExpression
        )?.takeIf { isWithCall }
}
