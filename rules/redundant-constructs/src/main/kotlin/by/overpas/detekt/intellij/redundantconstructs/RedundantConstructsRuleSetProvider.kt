package by.overpas.detekt.intellij.redundantconstructs

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class RedundantConstructsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-redundant-constructs")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
