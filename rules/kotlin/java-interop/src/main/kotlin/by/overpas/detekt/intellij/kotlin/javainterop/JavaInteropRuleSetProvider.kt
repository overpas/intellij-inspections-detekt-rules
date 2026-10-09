package by.overpas.detekt.intellij.kotlin.javainterop

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

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
