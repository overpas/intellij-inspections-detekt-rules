package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class StyleIssuesRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-style-issues")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::ReplaceAssertBooleanWithAssertEquality,
                ::ReplaceAssociateFunction,
                ::ReplaceCallWithBinaryOperator,
                ::ReplaceCollectionCountWithSize,
                ::ReplaceContains,
                ::ReplaceToStringWithStringTemplate,
                ::ReplaceToWithInfixForm,
                ::ReplaceUntilWithRangeUntil,
                ::ReplaceWithCallWithContextCall,
                ::ReplaceWithIgnoreCaseEquals,
                ::ReplaceWithImportAlias,
            ),
        )
}
