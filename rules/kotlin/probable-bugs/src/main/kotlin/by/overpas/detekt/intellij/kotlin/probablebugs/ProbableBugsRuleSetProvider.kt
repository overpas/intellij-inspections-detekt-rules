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
                ::SetterBackingFieldAssignment,
                ::SuspiciousCallOnCollectionToAddOrRemovePath,
                ::SuspiciousCascadingIf,
                ::SuspiciousCollectionReassignment,
                ::SuspiciousEqualsCombination,
            ),
        )
}
