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
    buildList {
        MATH_FUNCTIONS.mapTo(this) { "$MATH.$it" to ANY }
        MATH_EXTENSIONS.mapTo(this) { "$MATH.$it" to NON_NULL_RECEIVER }
        PRIMITIVES.mapTo(this) { "java.lang.$it.compare" to NON_NULL_RECEIVER }
        PRIMITIVES.mapTo(this) { "java.lang.$it.toString" to PRIMITIVE_TO_STRING }
        ARRAYS_NULLABLE_EXTENSIONS.mapTo(this) { "$ARRAYS.$it" to NULLABLE_RECEIVER }
        COLLECTIONS_MUTABLE_EXTENSIONS.mapTo(this) { "$COLLECTIONS.$it" to MUTABLE_LIST_RECEIVER }
        add("java.lang.Character.toString" to CHARACTER_TO_STRING)
        add("java.lang.System.exit" to ANY)
        add("java.io.PrintStream.print" to SYSTEM_OUT)
        add("java.io.PrintStream.println" to SYSTEM_OUT)
        add("$ARRAYS.copyOf" to TWO_ARGUMENTS_NON_NULL)
        add("$ARRAYS.copyOfRange" to NON_NULL_RECEIVER)
        add("$ARRAYS.equals" to TWO_ARGUMENTS)
        add("$ARRAYS.asList" to ANY)
        add("java.util.Set.of" to ANY)
        add("java.util.List.of" to ANY)
        add("$COLLECTIONS.binarySearch" to NON_NULL_RECEIVER)
        add("$COLLECTIONS.sort" to SORTABLE_LIST)
    }
        .groupBy { it.first.substringAfterLast('.') }
        .mapValues { it.value.toMap() }
