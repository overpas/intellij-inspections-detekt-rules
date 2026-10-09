package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDeclarationWithInitializer
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal fun KaSession.isSuspiciousCallContext(
    lambda: KtLambdaExpression,
    call: KaFunctionCall<*>?,
): Boolean {
    val argument = (lambda.parent as? ValueArgument)?.getArgumentExpression()
    val parameter = call?.run { valueArgumentMapping[argument] } ?: return true
    val returnType = (parameter.returnType as? KaFunctionType)?.returnType
    val originalReturnType = (parameter.symbol.returnType as? KaFunctionType)?.returnType
    return when {
        returnType != null && (returnType.isFunctionType || returnType.isSuspendFunctionType) -> false

        originalReturnType == null -> true

        else -> !originalReturnType.isSubtypeOf(StandardClassIds.Function) &&
            !originalReturnType.isSubtypeOf(StandardClassIds.KProperty)
    }
}

internal fun KaSession.isSuspiciousUsageContext(lambda: KtLambdaExpression): Boolean {
    val callElement: KtExpression = lambda.getStrictParentOfType<KtCallExpression>() ?: lambda
    if (!callElement.isUsedAsExpression) return true
    val qualifiedOrThis = callElement.getQualifiedExpressionForSelectorOrThis()
    val parentDeclaration = qualifiedOrThis.getStrictParentOfType<KtDeclaration>()
    val initializer = (parentDeclaration as? KtDeclarationWithInitializer)?.initializer
    val typeReference = (parentDeclaration as? KtCallableDeclaration)?.typeReference
    return qualifiedOrThis == initializer && typeReference == null
}
