package by.overpas.detekt.intellij.kotlin.codemigration

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

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
