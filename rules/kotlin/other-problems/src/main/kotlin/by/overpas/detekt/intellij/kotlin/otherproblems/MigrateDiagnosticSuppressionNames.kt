package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

private const val MIGRATE_DIAGNOSTIC_SUPPRESSION_QUOTE = "\""

private val migrateDiagnosticSuppressionNames = mapOf(
    "HEADER_DECLARATION_WITH_BODY" to "EXPECTED_DECLARATION_WITH_BODY",
    "HEADER_CLASS_CONSTRUCTOR_DELEGATION_CALL" to "EXPECTED_CLASS_CONSTRUCTOR_DELEGATION_CALL",
    "HEADER_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER" to "EXPECTED_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER",
    "HEADER_ENUM_CONSTRUCTOR" to "EXPECTED_ENUM_CONSTRUCTOR",
    "HEADER_ENUM_ENTRY_WITH_BODY" to "EXPECTED_ENUM_ENTRY_WITH_BODY",
    "HEADER_PROPERTY_INITIALIZER" to "EXPECTED_PROPERTY_INITIALIZER",
    "IMPL_TYPE_ALIAS_NOT_TO_CLASS" to "ACTUAL_TYPE_ALIAS_NOT_TO_CLASS",
    "IMPL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE" to
        "ACTUAL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE",
    "IMPL_TYPE_ALIAS_WITH_USE_SITE_VARIANCE" to "ACTUAL_TYPE_ALIAS_WITH_USE_SITE_VARIANCE",
    "IMPL_TYPE_ALIAS_WITH_COMPLEX_SUBSTITUTION" to "ACTUAL_TYPE_ALIAS_WITH_COMPLEX_SUBSTITUTION",
    "HEADER_WITHOUT_IMPLEMENTATION" to "NO_ACTUAL_FOR_EXPECT",
    "IMPLEMENTATION_WITHOUT_HEADER" to "ACTUAL_WITHOUT_EXPECT",
    "HEADER_CLASS_MEMBERS_ARE_NOT_IMPLEMENTED" to "NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS",
    "IMPL_MISSING" to "ACTUAL_MISSING",
)

internal fun KtAnnotationEntry.obsoleteDiagnosticNames(): Sequence<Pair<KtStringTemplateExpression, String>> =
    valueArguments
        .asSequence()
        .mapNotNull { it.getArgumentExpression() as? KtStringTemplateExpression }
        .filter { expression ->
            expression.text.length > 1 &&
                expression.text.startsWith(MIGRATE_DIAGNOSTIC_SUPPRESSION_QUOTE) &&
                expression.text.endsWith(MIGRATE_DIAGNOSTIC_SUPPRESSION_QUOTE)
        }
        .mapNotNull { expression ->
            migrateDiagnosticSuppressionNames[expression.text.removeSurrounding(MIGRATE_DIAGNOSTIC_SUPPRESSION_QUOTE)]
                ?.let { expression to it }
        }
