package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class ProbableBugsRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("intellij-kotlin-probable-bugs")

    override fun instance() =
        RuleSet(
            ruleSetId,
            listOf(
                ::AmbiguousNonLocalJump,
                ::ArrayInDataClass,
                ::AssignedValueIsNeverRead,
                ::CanSealedSubClassBeObject,
                ::ConflictingExtensionProperty,
                ::ConvertNaNEquality,
                ::DataClassPrivateConstructor,
                ::DelegationToVarProperty,
                ::DuplicateArgumentsInSetOfAndMapOfFunctions,
                ::EmptyRange,
                ::FilterIsInstanceResultIsAlwaysEmpty,
                ::ForEachParameterNotUsed,
                ::ImplicitNullableNothingType,
                ::IncompleteDestructuring,
                ::JavaIoSerializableObjectMustHaveReadResolve,
                ::KotlinArrayHashCode,
                ::KotlinArrayToString,
                ::KotlinEqualsBetweenInconvertibleTypes,
                ::KotlinMisorderedAssertEqualsArguments,
                ::KotlinThrowableNotThrown,
                ::LateinitVarOverridesLateinitVar,
                ::LazyWithoutDelegation,
                ::MainFunctionReturnUnit,
                ::RecursiveEqualsCall,
                ::RecursivePropertyAccessor,
                ::RedundantLabel,
                ::SelfAssignment,
                ::SelfReferenceConstructorParameter,
                ::SetterBackingFieldAssignment,
                ::SuspiciousCallOnCollectionToAddOrRemovePath,
                ::SuspiciousCascadingIf,
                ::SuspiciousCollectionReassignment,
                ::SuspiciousEqualsCombination,
            ),
        )
}
