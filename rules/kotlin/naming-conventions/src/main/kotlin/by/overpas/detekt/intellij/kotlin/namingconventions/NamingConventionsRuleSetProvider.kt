package by.overpas.detekt.intellij.kotlin.namingconventions

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class NamingConventionsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-naming-conventions")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
