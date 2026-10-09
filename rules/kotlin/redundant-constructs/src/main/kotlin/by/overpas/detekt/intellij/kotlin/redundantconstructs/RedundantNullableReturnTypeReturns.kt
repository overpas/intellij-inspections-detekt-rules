package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isExpectDeclaration

private val TRANSIENT = ClassId.fromString("kotlin/jvm/Transient")

private val JSPECIFY_NULLABLE = setOf(
    ClassId.fromString("org/jspecify/annotations/Nullable"),
    ClassId.fromString("org/jspecify/nullness/Nullable"),
)

internal fun KtCallableDeclaration.nullableReturnTypeQuestionMark(): PsiElement? {
    val isExpectOrActual = isExpectDeclaration() ||
        hasModifier(KtTokens.ACTUAL_KEYWORD) ||
        containingClassOrObject?.hasModifier(KtTokens.ACTUAL_KEYWORD) == true
    val nullableType = (typeReference?.typeElement as? KtNullableType)?.takeIf { it.innerType != null }
    return nullableType?.takeUnless { isExpectOrActual }?.run { questionMarkNode.psi }
}

internal fun KtCallableDeclaration.isOverridableCallable(): Boolean =
    !hasModifier(KtTokens.PRIVATE_KEYWORD) &&
        analyze(this) { (symbol as? KaCallableSymbol)?.run { modality != KaSymbolModality.FINAL } == true }

internal fun KtCallableDeclaration.hasTransientBackingField(): Boolean =
    analyze(this) {
        (symbol as? KaPropertySymbol)?.backingFieldSymbol?.run { annotations.any { it.classId == TRANSIENT } } == true
    }

internal fun KtCallableDeclaration.hasOnlyNonNullReturnTypes(): Boolean {
    val nonNullFlags = when (this) {
        is KtNamedFunction -> nonNullReturnFlags(bodyExpression)
        is KtProperty -> nonNullReturnFlags(initializer) + getter?.run { nonNullReturnFlags(bodyExpression) }.orEmpty()
        else -> emptyList()
    }
    return nonNullFlags.isNotEmpty() && nonNullFlags.all { it }
}

@OptIn(KaExperimentalApi::class)
private fun KtDeclaration.nonNullReturnFlags(body: KtExpression?): List<Boolean> =
    body?.let { bodyExpression ->
        analyze(this) {
            val target = symbol
            val returned = bodyExpression
                .collectDescendantsOfType<KtReturnExpression> { it.resolveSymbol() == target }
                .map { it.returnedExpression }
            (returned + bodyExpression.takeUnless { it is KtBlockExpression })
                .mapNotNull { it?.expressionType }
                .map { type -> !type.isNullable && type.annotations.classIds.none { it in JSPECIFY_NULLABLE } }
        }
    }.orEmpty()
