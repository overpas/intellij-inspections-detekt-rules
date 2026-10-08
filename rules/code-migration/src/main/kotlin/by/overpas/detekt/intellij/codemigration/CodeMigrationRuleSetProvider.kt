package by.overpas.detekt.intellij.codemigration

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class CodeMigrationRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-code-migration")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
