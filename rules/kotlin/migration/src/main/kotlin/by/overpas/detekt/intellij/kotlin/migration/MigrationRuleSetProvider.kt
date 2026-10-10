package by.overpas.detekt.intellij.kotlin.migration

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.migration")
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
