package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor

private val INCREMENTS = setOf(KtTokens.PLUSPLUS, KtTokens.MINUSMINUS)

internal fun KtSimpleNameExpression.isRecursiveAccessorAccess(): Boolean {
    val accessor = getParentOfType<KtDeclarationWithBody>(true) as? KtPropertyAccessor
    return accessor != null &&
        text == accessor.property.name &&
        isSameAccessor(accessor.isGetter) &&
        analyze(this) { recursiveAccessorTargets(accessor) }
}

internal fun KtSimpleNameExpression.isRecursiveSyntheticAccess(): Boolean {
    val function = getParentOfType<KtDeclarationWithBody>(true) as? KtNamedFunction
    val referencedName = text.replaceFirstChar { if (it in 'a'..'z') it.uppercaseChar() else it }
    val isGetter = function?.name == "get$referencedName"
    val isSetter = function?.name == "set$referencedName"
    return function != null &&
        (isGetter || isSetter) &&
        isSameAccessor(isGetter) &&
        analyze(this) { recursiveSyntheticTargets(function) }
}

private fun KtSimpleNameExpression.isSameAccessor(isGetter: Boolean): Boolean {
    val assignment = getStrictParentOfType<KtBinaryExpression>()
        ?.takeIf { KtPsiUtil.isAssignment(it) && it.left?.isAncestor(this) == true }
    val isIncrement = getStrictParentOfType<KtUnaryExpression>()?.operationToken in INCREMENTS
    return if (isGetter) {
        assignment == null || assignment.operationToken in KtTokens.AUGMENTED_ASSIGNMENTS
    } else {
        assignment != null || isIncrement
    }
}
