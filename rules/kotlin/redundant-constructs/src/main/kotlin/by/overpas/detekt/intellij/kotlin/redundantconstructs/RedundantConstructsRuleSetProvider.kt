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
                ::CanBeParameter,
                ::CanBePrimaryConstructorProperty,
                ::CanUnescapeDollarLiteral,
                ::ConstantConditionIf,
                ::ExplicitThis,
                ::IfExpressionWithIdenticalBranches,
                ::KotlinRedundantOverride,
                ::NullChecksToSafeCall,
                ::RedundantCompanionReference,
                ::RedundantElvisReturnNull,
                ::RedundantEnumConstructorInvocation,
                ::RedundantGetter,
                ::RedundantIf,
                ::RedundantInterpolationPrefix,
                ::RedundantLabeledReturnOnLastExpressionInLambda,
                ::RedundantLambdaArrow,
                ::RedundantLambdaOrAnonymousFunction,
                ::RedundantModalityModifier,
                ::RedundantNullableReturnType,
                ::RedundantReturnKeyword,
                ::RedundantReturnLabel,
                ::RedundantSamConstructor,
                ::RedundantSetter,
                ::RedundantUpperBound,
                ::RedundantValueArgument,
                ::RedundantWith,
                ::RemoveExplicitSuperQualifier,
                ::RemoveExplicitTypeArguments,
                ::RemoveRedundantQualifierName,
                ::RemoveRedundantSpreadOperator,
                ::RemoveSetterParameterType,
                ::RemoveSingleExpressionStringTemplate,
                ::ScopeFunctionConversion,
                ::SimplifyWhenWithBooleanConstantCondition,
                ::SuspiciousCallableReferenceInLambda,
                ::UnnecessaryOptInAnnotation,
                ::UnnecessaryVariable,
                ::UnusedContextParameterCall,
                ::UnusedReceiverParameter,
                ::VariableInitializerIsRedundant,
                ::WhenWithOnlyElse,
            ),
        )
}
