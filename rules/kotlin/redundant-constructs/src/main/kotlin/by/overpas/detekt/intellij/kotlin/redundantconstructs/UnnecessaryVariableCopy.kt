package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaLocalVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.nextLeafs
import org.jetbrains.kotlin.psi.psiUtil.prevLeafs

private const val IMPLICIT_PARAMETER = "it"

internal class UnnecessaryVariableCopy(
    private val session: KaSession,
    private val property: KtProperty,
    private val initializer: KtNameReferenceExpression,
) {

    private val enclosing = KtPsiUtil.getEnclosingElementForLocalDeclaration(property)

    private val source: KaVariableSymbol? =
        with(session) { initializer.resolveToCall()?.successfulVariableAccessCall()?.symbol }

    fun isPlainDeclaration(): Boolean =
        !property.isVar &&
            property.typeReference == null &&
            property.annotationEntries.isEmpty() &&
            !property.hasDelegate() &&
            !hasCommentBefore() &&
            !hasCommentAfter()

    fun hasCopyableSource(): Boolean =
        with(session) {
            val symbol = source
            (symbol is KaLocalVariableSymbol || symbol is KaParameterSymbol) &&
                symbol.isVal &&
                symbol.containingSymbol is KaFunctionSymbol &&
                (symbol.psi as? KtProperty)?.hasDelegate() != true
        }

    fun isUsed(): Boolean =
        with(session) {
            val symbol = property.symbol
            enclosing?.anyDescendantOfType<KtNameReferenceExpression> { reference ->
                reference.getReferencedName() == property.name &&
                    reference.resolveToCall()?.successfulVariableAccessCall()?.symbol == symbol
            } == true
        }

    fun hasNameConflict(): Boolean {
        val name = initializer.getReferencedName()
        val sourcePsi = source?.psi
        val hasDeclaration = enclosing?.anyDescendantOfType<KtNamedDeclaration> {
            it.name == name && it != sourcePsi
        } == true
        val hasImplicitParameter = name == IMPLICIT_PARAMETER &&
            enclosing?.anyDescendantOfType<KtLambdaExpression> { it.hasImplicitParameter() } == true
        return hasDeclaration || hasImplicitParameter
    }

    private fun KtLambdaExpression.hasImplicitParameter(): Boolean =
        with(session) {
            functionLiteral.valueParameterList == null && functionLiteral.symbol.valueParameters.size == 1
        }

    private fun hasCommentBefore(): Boolean =
        property.prevLeafs.takeWhile { it is PsiWhiteSpace || it is PsiComment }.any { it is PsiComment }

    private fun hasCommentAfter(): Boolean =
        initializer.nextLeafs
            .takeWhile { it is PsiComment || (it is PsiWhiteSpace && !it.textContains('\n')) }
            .any { it is PsiComment }
}
