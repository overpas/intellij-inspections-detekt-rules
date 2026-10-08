package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class ProbableBugsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-probable-bugs")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
