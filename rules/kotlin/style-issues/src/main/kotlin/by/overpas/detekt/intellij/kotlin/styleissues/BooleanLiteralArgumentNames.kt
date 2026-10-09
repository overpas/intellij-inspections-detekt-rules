package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgument

private val BOOLEAN_LITERAL_ARGUMENT_IGNORED_CLASSES =
    listOf("kotlin.Pair", "kotlin.Triple").map { ClassId.topLevel(FqName(it)) }

context(session: KaSession)
internal fun KtValueArgument.hasBooleanLiteralStableName(call: KtCallExpression): Boolean =
    with(session) {
        val functionCall = call.resolveToCall()?.successfulFunctionCallOrNull()
        val function = functionCall?.symbol
        val expression = getArgumentExpression()
        val parameter = functionCall?.run { valueArgumentMapping[expression]?.symbol }
        val vararg = function?.run { valueParameters.find { it.isVararg } }
        val isAmbiguousVararg = vararg != null &&
            functionCall.valueArgumentMapping.values.count { it.symbol == vararg } > 1 &&
            expression?.expressionType?.isSubtypeOf(vararg.returnType) == true
        parameter != null &&
            !parameter.isVararg &&
            !isAmbiguousVararg &&
            function?.hasStableParameterNames == true &&
            (function as? KaConstructorSymbol)?.containingClassId !in BOOLEAN_LITERAL_ARGUMENT_IGNORED_CLASSES
    }
