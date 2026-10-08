package by.overpas.detekt.intellij.coroutines

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class CoroutinesRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-coroutines")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
