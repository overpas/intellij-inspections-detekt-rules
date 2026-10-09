package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.util.OperatorNameConventions

internal fun KtBinaryExpression.isUnusedEqualsCandidate(): Boolean =
    operationToken == KtTokens.EQEQ && (parent.parent is KtIfExpression || parent is KtBlockExpression)

internal fun KtCallExpression.unusedEqualsCallTarget(): KtExpression? =
    takeIf {
        (calleeExpression as? KtSimpleNameExpression)?.getReferencedNameAsName() == OperatorNameConventions.EQUALS
    }
        ?.takeIf { call ->
            analyze(call) {
                val symbol = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol as? KaNamedFunctionSymbol
                symbol != null &&
                    symbol.isOverride &&
                    symbol.valueParameters.singleOrNull()?.run {
                        returnType.isAnyType && returnType.isMarkedNullable
                    } ==
                    true &&
                    symbol.returnType.run { isBooleanType && !isMarkedNullable }
            }
        }
        ?.getQualifiedExpressionForSelectorOrThis()

internal fun KtExpression.isUnusedEqualsResult(): Boolean =
    analyze(this) { !isUsedAsExpression }
