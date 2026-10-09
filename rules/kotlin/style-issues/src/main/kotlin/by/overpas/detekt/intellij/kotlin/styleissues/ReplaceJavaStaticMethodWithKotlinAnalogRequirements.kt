package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.ANY
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.CHARACTER_TO_STRING
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.MUTABLE_LIST_RECEIVER
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.NON_NULL_RECEIVER
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.NULLABLE_RECEIVER
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.PRIMITIVE_TO_STRING
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.SORTABLE_LIST
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.SYSTEM_OUT
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.TWO_ARGUMENTS
import by.overpas.detekt.intellij.kotlin.styleissues.ReplaceJavaStaticMethodWithKotlinAnalogRequirement.TWO_ARGUMENTS_NON_NULL

private const val MATH = "java.lang.Math"

private const val ARRAYS = "java.util.Arrays"

private const val COLLECTIONS = "java.util.Collections"

private val PRIMITIVES = listOf("Integer", "Long", "Byte", "Character", "Short", "Double", "Float")

private val MATH_FUNCTIONS = listOf(
    "abs", "acos", "asin", "atan", "atan2", "cbrt", "ceil", "cos", "cosh", "exp", "expm1", "floor", "hypot",
    "log", "log1p", "log10", "max", "min", "rint", "signum", "sin", "sinh", "sqrt", "tan", "tanh",
)

private val MATH_EXTENSIONS = listOf("IEEEremainder", "nextDown", "nextAfter", "nextUp", "pow", "round", "copySign")

private val ARRAYS_NULLABLE_EXTENSIONS = listOf("deepEquals", "deepHashCode", "hashCode", "deepToString", "toString")

private val COLLECTIONS_MUTABLE_EXTENSIONS = listOf("reverse", "fill", "shuffle")

internal val REPLACE_JAVA_STATIC_METHOD_REQUIREMENTS =
    (
        MATH_FUNCTIONS.map { "$MATH.$it" to ANY } +
            MATH_EXTENSIONS.map { "$MATH.$it" to NON_NULL_RECEIVER } +
            PRIMITIVES.map { "java.lang.$it.compare" to NON_NULL_RECEIVER } +
            PRIMITIVES.map { "java.lang.$it.toString" to PRIMITIVE_TO_STRING } +
            ARRAYS_NULLABLE_EXTENSIONS.map { "$ARRAYS.$it" to NULLABLE_RECEIVER } +
            COLLECTIONS_MUTABLE_EXTENSIONS.map { "$COLLECTIONS.$it" to MUTABLE_LIST_RECEIVER } +
            listOf(
                "java.lang.Character.toString" to CHARACTER_TO_STRING,
                "java.lang.System.exit" to ANY,
                "java.io.PrintStream.print" to SYSTEM_OUT,
                "java.io.PrintStream.println" to SYSTEM_OUT,
                "$ARRAYS.copyOf" to TWO_ARGUMENTS_NON_NULL,
                "$ARRAYS.copyOfRange" to NON_NULL_RECEIVER,
                "$ARRAYS.equals" to TWO_ARGUMENTS,
                "$ARRAYS.asList" to ANY,
                "java.util.Set.of" to ANY,
                "java.util.List.of" to ANY,
                "$COLLECTIONS.binarySearch" to NON_NULL_RECEIVER,
                "$COLLECTIONS.sort" to SORTABLE_LIST,
            )
        )
        .groupBy { it.first.substringAfterLast('.') }
        .mapValues { it.value.toMap() }
