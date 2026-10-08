package by.overpas.detekt.intellij.javainterop

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class JavaInteropRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-java-interop")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
