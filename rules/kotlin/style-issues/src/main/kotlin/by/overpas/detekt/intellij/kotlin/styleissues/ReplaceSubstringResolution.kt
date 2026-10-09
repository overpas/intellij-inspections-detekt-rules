package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression

context(session: KaSession)
internal fun KtExpression.isReplaceSubstringResolvedTo(fqName: String): Boolean =
    with(session) { resolveToCall()?.successfulFunctionCallOrNull() }
        ?.symbol
        ?.callableId
        ?.run { asSingleFqName().asString() } == fqName

context(session: KaSession)
internal fun KtExpression.replaceSubstringIntValue(): Int? =
    with(session) { evaluate() }?.value as? Int

context(session: KaSession)
internal fun KtExpression?.isReplaceSubstringIndexOfOn(receiver: KtExpression): Boolean =
    this is KtDotQualifiedExpression &&
        receiverExpression.text == receiver.text &&
        (selectorExpression as? KtCallExpression)?.run { valueArguments.size } == 1 &&
        isReplaceSubstringResolvedTo("kotlin.text.indexOf")

context(session: KaSession)
internal fun KtDotQualifiedExpression.isReplaceSubstringStdlibCall(): Boolean =
    isReplaceSubstringResolvedTo("kotlin.text.substring")
