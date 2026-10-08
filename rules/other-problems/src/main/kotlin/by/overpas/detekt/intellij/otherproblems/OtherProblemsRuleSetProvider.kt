package by.overpas.detekt.intellij.otherproblems

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class OtherProblemsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-other-problems")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
