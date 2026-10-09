package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.startOffset

private const val OBJECT_PREFIX = "object __Obj__ {"

internal fun KtCallExpression.withoutTypeArgumentsInFragment(): KtCallExpression? {
    val context = parentsWithSelf.filterIsInstance<KtDeclaration>().firstOrNull { it.isAnalysisContext }
    val range = typeArgumentList?.textRange
    if (context == null || range == null) return null
    val contextStart = context.startOffset
    val text = context.text.removeRange(range.startOffset - contextStart, range.endOffset - contextStart)
    val hasAccessor = parentsWithSelf.drop(1).takeWhile { it != context }.any { it is KtPropertyAccessor }
    val prefix = if (hasAccessor) OBJECT_PREFIX else ""
    val suffix = if (hasAccessor) "}" else ""
    val fragment = KtPsiFactory(project, markGenerated = false).createBlockCodeFragment("$prefix$text$suffix", context)
    return fragment.findElementAt(range.startOffset + prefix.length - contextStart)?.parentOfType()
}

private val KtDeclaration.isAnalysisContext: Boolean
    get() = when (this) {
        is KtFunctionLiteral, is KtParameter, is KtPropertyAccessor -> false
        is KtProperty -> !isLocal
        is KtFunction -> !hasModifier(KtTokens.OVERRIDE_KEYWORD)
        else -> true
    }
