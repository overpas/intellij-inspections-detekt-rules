package by.overpas.detekt.intellij.kotlin.logging

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.logging")
class LoggingRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-logging")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::KotlinLoggerInitializedWithForeignClass,
            ),
        )
}
