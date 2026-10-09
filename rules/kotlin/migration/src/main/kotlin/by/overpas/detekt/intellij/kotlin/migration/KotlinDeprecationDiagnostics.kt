package by.overpas.detekt.intellij.kotlin.migration

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.diagnostics.KaDiagnosticWithPsi
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.psi.KtFile

private val KOTLIN_DEPRECATION_CHECKER_FILTERS = listOf(
    KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS,
    KaDiagnosticCheckerFilter.ONLY_EXTENDED_CHECKERS,
)

context(session: KaSession)
internal fun KtFile.cleanupDiagnostics(): List<Pair<PsiElement, String>> =
    KOTLIN_DEPRECATION_CHECKER_FILTERS
        .asSequence()
        .flatMap { with(session) { collectDiagnostics(it) } }
        .filter { it.isCleanup() }
        .map { it.psi to it.defaultMessage }
        .toList()

context(session: KaSession)
private fun KaDiagnosticWithPsi<*>.isCleanup(): Boolean =
    when (this) {
        is KaFirDiagnostic.Deprecation -> reference.hasDeprecationReplacement()

        is KaFirDiagnostic.DeprecationError -> reference.hasDeprecationReplacement()

        is KaFirDiagnostic.CommaInWhenConditionWithoutArgument,
        is KaFirDiagnostic.DeprecatedTypeParameterSyntax,
        is KaFirDiagnostic.InfixModifierRequired,
        is KaFirDiagnostic.MisplacedTypeParameterConstraints,
        is KaFirDiagnostic.MissingConstructorKeyword,
        is KaFirDiagnostic.NonConstValUsedInConstantExpression,
        is KaFirDiagnostic.OperatorModifierRequired,
        is KaFirDiagnostic.PositionedValueArgumentForJavaAnnotation,
        is KaFirDiagnostic.UnnecessaryNotNullAssertion,
        is KaFirDiagnostic.UnnecessarySafeCall,
        is KaFirDiagnostic.UselessCast,
        is KaFirDiagnostic.UselessElvis,
        -> true

        else -> false
    }
