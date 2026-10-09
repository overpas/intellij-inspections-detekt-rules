package by.overpas.detekt.intellij.kotlin.migration

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class MigrationRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-migration")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::KotlinDeprecation,
            ),
        )
}
