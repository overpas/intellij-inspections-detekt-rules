package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtExpressionWithLabel
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

context(session: KaSession)
internal fun SimplifyNestedEachInScopeFunctionCall.message(): String? {
    val scopeName = scopeCall.resolvedNestedEachName()
    val eachName = eachCall?.resolvedNestedEachName()
    val isSimplifiable =
        eachLambdaBody?.anyDescendantOfType<KtExpressionWithLabel> { it.getLabelName() == labelName } != true &&
            when (scopeName) {
                SIMPLIFY_NESTED_EACH_ALSO -> isSimplifiableAlso()
                SIMPLIFY_NESTED_EACH_APPLY -> isSimplifiableApply()
                else -> false
            }
    return if (scopeName != null && eachName != null && isSimplifiable) {
        "Nested '$eachName' call in '$scopeName' could be simplified to $SIMPLIFY_NESTED_EACH_ON_EACH"
    } else {
        null
    }
}
