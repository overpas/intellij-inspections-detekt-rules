package by.overpas.detekt.intellij.numericissues

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class NumericIssuesRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-numeric-issues")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
