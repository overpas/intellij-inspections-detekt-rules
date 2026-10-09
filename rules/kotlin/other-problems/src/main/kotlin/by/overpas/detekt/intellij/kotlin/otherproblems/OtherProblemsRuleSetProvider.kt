package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

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
