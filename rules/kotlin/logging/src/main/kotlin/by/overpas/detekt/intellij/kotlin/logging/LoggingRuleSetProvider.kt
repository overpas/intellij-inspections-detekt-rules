package by.overpas.detekt.intellij.kotlin.logging

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class LoggingRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-logging")

    override fun instance() =
        RuleSet(ruleSetId, emptyList())
}
