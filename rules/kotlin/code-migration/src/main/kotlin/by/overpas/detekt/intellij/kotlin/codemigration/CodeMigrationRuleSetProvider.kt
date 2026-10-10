package by.overpas.detekt.intellij.kotlin.codemigration

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.code.migration")
class CodeMigrationRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-code-migration")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::CanConvertToMultiDollarString,
                ::ConvertFromMultiDollarToRegularString,
                ::ConvertLongToDuration,
                ::InfixCallToOrdinary,
            ),
        )
}
