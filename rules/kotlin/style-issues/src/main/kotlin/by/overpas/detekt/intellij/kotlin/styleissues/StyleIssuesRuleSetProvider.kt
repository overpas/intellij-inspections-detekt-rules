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
                ::AddOperatorModifier,
                ::AddVarianceModifier,
                ::BooleanLiteralArgument,
                ::CascadeIf,
                ::IfThenToElvis,
                ::IfThenToSafeAccess,
                ::ImplicitThis,
                ::IntroduceWhenSubject,
                ::JavaMapForEach,
                ::JoinDeclarationAndAssignment,
                ::RedundantElseInIf,
                ::RedundantObjectTypeCheck,
                ::RedundantRunCatching,
                ::RemoveEmptyParenthesesFromAnnotationEntry,
                ::ReplaceAddAllWithMapTo,
                ::ReplaceAssertBooleanWithAssertEquality,
                ::ReplaceAssociateFunction,
                ::ReplaceCallWithBinaryOperator,
                ::ReplaceCollectionCountWithSize,
                ::ReplaceContains,
                ::ReplaceIfExpressionWithFirstOrNull,
                ::ReplaceJavaStaticMethodWithKotlinAnalog,
                ::ReplaceManualRangeWithIndicesCalls,
                ::ReplaceStringFormatWithLiteral,
                ::ReplaceSubstringWithDropLast,
                ::ReplaceSubstringWithIndexingOperation,
                ::ReplaceSubstringWithSubstringAfter,
                ::ReplaceSubstringWithSubstringBefore,
                ::ReplaceSubstringWithTake,
                ::ReplaceToStringWithStringTemplate,
                ::ReplaceToWithInfixForm,
                ::ReplaceUntilWithRangeUntil,
                ::ReplaceWithCallWithContextCall,
                ::ReplaceWithIgnoreCaseEquals,
                ::ReplaceWithImportAlias,
            ),
        )
}
