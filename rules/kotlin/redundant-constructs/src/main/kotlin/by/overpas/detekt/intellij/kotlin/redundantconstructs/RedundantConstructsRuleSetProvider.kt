package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class RedundantConstructsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-redundant-constructs")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::RedundantGetter,
                ::RedundantLabeledReturnOnLastExpressionInLambda,
                ::RedundantLambdaOrAnonymousFunction,
                ::RemoveSetterParameterType,
                ::SimplifyWhenWithBooleanConstantCondition,
                ::WhenWithOnlyElse,
            ),
        )
}
