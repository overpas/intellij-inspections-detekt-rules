package by.overpas.detekt.intellij.kotlin.javainterop

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.java.interop.issues")
class JavaInteropRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-java-interop")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::JavaCollectionWithNullableTypeArgument,
                ::JavaCollectionsStaticMethodOnImmutableList,
                ::JavaDefaultMethodsNotOverriddenByDelegation,
            ),
        )
}
