package by.overpas.detekt.intellij.kotlin.probablebugs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

private val LAZY_WITHOUT_DELEGATION_LAZY_CLASS_ID = ClassId.topLevel(FqName("kotlin.Lazy"))

private val LAZY_WITHOUT_DELEGATION_VALUE_ID = CallableId(
    LAZY_WITHOUT_DELEGATION_LAZY_CLASS_ID,
    Name.identifier("value"),
)

internal class LazyWithoutDelegationUsages(private val property: KtProperty) {

    private val KtNameReferenceExpression.target: PsiElement?
        get() = analyze(this) {
            references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }?.psi
        }

    private val KtExpression.outermostParenthesized: KtExpression
        get() = generateSequence(this) { it.parent as? KtParenthesizedExpression }.last()

    private val KtExpression.outermostAccess: KtExpression
        get() = generateSequence(outermostParenthesized) { expression ->
            (expression.getQualifiedExpressionForSelector() as? KtDotQualifiedExpression)?.outermostParenthesized
        }.last()

    private val KtExpression.isLazyValueRead: Boolean
        get() {
            val qualified = (parent as? KtDotQualifiedExpression)?.takeIf { it.receiverExpression == this }
            val selector = qualified?.selectorExpression ?: return false
            return analyze(selector) {
                selector.resolveToCall()?.successfulVariableAccessCall()?.run { symbol.callableId } ==
                    LAZY_WITHOUT_DELEGATION_VALUE_ID
            }
        }

    private val isLazy: Boolean
        get() = analyze(property) {
            property.initializer?.expressionType?.isSubtypeOf(LAZY_WITHOUT_DELEGATION_LAZY_CLASS_ID) == true
        }

    val areOnlyValueReads: Boolean
        get() = isLazy &&
            property.containingKtFile
                .collectDescendantsOfType<KtNameReferenceExpression> {
                    it.getReferencedName() == property.name && it.target == property
                }
                .all { it.outermostAccess.isLazyValueRead }
}

internal val KtProperty.isLazyWithoutDelegationCandidate: Boolean
    get() = !isVar &&
        (isLocal || isPrivate()) &&
        !hasDelegate() &&
        typeReference == null &&
        initializer != null &&
        annotationEntries.isEmpty()
