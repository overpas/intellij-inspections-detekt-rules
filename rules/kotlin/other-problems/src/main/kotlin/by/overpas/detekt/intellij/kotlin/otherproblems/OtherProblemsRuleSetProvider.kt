package by.overpas.detekt.intellij.kotlin.otherproblems

import by.overpas.detekt.intellij.IntellijInspectionGroup
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

@IntellijInspectionGroup("group.names.other.problems")
class OtherProblemsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-other-problems")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::ConvertArgumentToSet,
                ::DeprecatedCallableAddReplaceWith,
                ::EnumValuesSoftDeprecate,
                ::EnumValuesTopLevelFunctionSoftDeprecate,
                ::FloatingPointLiteralPrecision,
                ::MigrateDiagnosticSuppression,
                ::ReplaceWithEnumMap,
                ::ReplaceWithStringBuilderAppendRange,
            ),
        )
}
