package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression

internal val KOTLIN_ARRAY_TO_STRING_CALLABLE_ID = CallableId(StandardClassIds.Any, Name.identifier("toString"))

private val KOTLIN_ARRAY_TO_STRING_CHAR_ARRAY_CLASS_ID =
    StandardClassIds.primitiveArrayTypeByElementType.getValue(StandardClassIds.Char)

private val KOTLIN_ARRAY_TO_STRING_APPEND = Name.identifier("append")

private val KOTLIN_ARRAY_TO_STRING_PRINTS = listOf(Name.identifier("print"), Name.identifier("println"))

private val KOTLIN_ARRAY_TO_STRING_BUILDERS =
    listOf("java.lang.StringBuilder", "java.lang.StringBuffer").map { ClassId.topLevel(FqName(it)) }

private val KOTLIN_ARRAY_TO_STRING_WRITERS =
    listOf("java.io.PrintStream", "java.io.PrintWriter").map { ClassId.topLevel(FqName(it)) }

private val KOTLIN_ARRAY_TO_STRING_IMPLICIT_CALLABLE_IDS =
    KOTLIN_ARRAY_TO_STRING_BUILDERS.map { builder -> CallableId(builder, KOTLIN_ARRAY_TO_STRING_APPEND) } +
        KOTLIN_ARRAY_TO_STRING_PRINTS.flatMap { print ->
            KOTLIN_ARRAY_TO_STRING_WRITERS.map { writer -> CallableId(writer, print) } +
                CallableId(FqName("kotlin.io"), print)
        }

private val KOTLIN_ARRAY_TO_STRING_IMPLICIT_NAMES =
    KOTLIN_ARRAY_TO_STRING_IMPLICIT_CALLABLE_IDS.map { it.callableName.asString() }.toSet()

internal fun KtCallExpression.implicitToStringCalleeOrNull(): KtExpression? =
    calleeExpression?.takeIf { it.text in KOTLIN_ARRAY_TO_STRING_IMPLICIT_NAMES }

context(session: KaSession)
internal fun KtBinaryExpression.isStringPlusArray(): Boolean =
    with(session) {
        left?.expressionType?.isClassType(StandardClassIds.String) == true &&
            right?.expressionType?.isArrayOrPrimitiveArray == true
    }

context(session: KaSession)
internal fun KtCallExpression.hasImplicitArrayToString(): Boolean =
    with(session) {
        val function = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
        val argumentType = valueArguments.singleOrNull()?.getArgumentExpression()?.expressionType
        function?.callableId in KOTLIN_ARRAY_TO_STRING_IMPLICIT_CALLABLE_IDS &&
            argumentType?.isArrayOrPrimitiveArray == true &&
            !argumentType.isClassType(KOTLIN_ARRAY_TO_STRING_CHAR_ARRAY_CLASS_ID)
    }
