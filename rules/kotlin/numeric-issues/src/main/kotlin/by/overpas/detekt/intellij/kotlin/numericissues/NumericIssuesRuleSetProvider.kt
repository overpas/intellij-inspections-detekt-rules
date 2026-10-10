package by.overpas.detekt.intellij.kotlin.numericissues

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.numeric.issues")
class NumericIssuesRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-numeric-issues")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::KotlinBigDecimalEquals,
            ),
        )
}
