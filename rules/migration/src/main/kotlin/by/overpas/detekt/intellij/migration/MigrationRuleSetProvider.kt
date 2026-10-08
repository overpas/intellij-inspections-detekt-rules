package by.overpas.detekt.intellij.migration

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class MigrationRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-migration")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
