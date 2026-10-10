package by.overpas.detekt.intellij.kotlin.namingconventions

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.naming.conventions")
class NamingConventionsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-naming-conventions")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::InconsistentCommentForJavaParameter,
                ::LocalVariableName,
            ),
        )
}
