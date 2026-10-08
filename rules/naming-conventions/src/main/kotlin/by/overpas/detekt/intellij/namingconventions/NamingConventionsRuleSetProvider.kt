package by.overpas.detekt.intellij.namingconventions

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class NamingConventionsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-naming-conventions")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
