package by.overpas.detekt.intellij.kotlin.numericissues

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class NumericIssuesRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-numeric-issues")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
