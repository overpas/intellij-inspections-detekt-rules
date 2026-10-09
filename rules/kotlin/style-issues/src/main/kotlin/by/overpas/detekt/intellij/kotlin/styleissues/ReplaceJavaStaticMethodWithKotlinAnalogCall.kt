package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.singleCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private const val SYSTEM_OUT = "java.lang.System.out"

private val VALID_RADIXES = 2..36

internal class ReplaceJavaStaticMethodWithKotlinAnalogCall(private val call: KtCallExpression) {

    private val requirements = REPLACE_JAVA_STATIC_METHOD_REQUIREMENTS[call.calleeExpression?.text]

    private val arguments = call.valueArguments.map { it.getArgumentExpression() }

    private val radix = arguments.lastOrNull()

    private val hasValidRadix = radix !is KtConstantExpression || radix.text.toIntOrNull() in VALID_RADIXES

    private val printStream = (call.parent as? KtQualifiedExpression)
        ?.takeIf { it.selectorExpression == call }
        ?.receiverExpression

    fun isReplaceable(): Boolean =
        requirements?.let { candidates ->
            analyze(call) {
                val symbol = call.resolveToCall()?.singleFunctionCallOrNull()?.symbol
                val requirement = candidates[symbol?.callableId?.javaStaticMethodName]
                val firstType = arguments.firstOrNull()?.expressionType
                val printStreamCall = printStream?.resolveToCall()?.singleCallOrNull<KaVariableAccessCall>()
                val facts = ReplaceJavaStaticMethodWithKotlinAnalogFacts(
                    argumentCount = arguments.size,
                    hasValidRadix = hasValidRadix,
                    hasLambdaSecondArgument = arguments.getOrNull(1) is KtLambdaExpression,
                    isFirstArgumentNullable = firstType?.isMarkedNullable,
                    isFirstArgumentMutableList = firstType?.isSubtypeOf(StandardClassIds.MutableList) == true,
                    isFirstArgumentChar = (firstType as? KaClassType)?.classId == StandardClassIds.Char,
                    isSystemOut = printStreamCall?.symbol?.callableId?.javaStaticMethodName == SYSTEM_OUT,
                )
                requirement?.isMet(facts) == true
            }
        } == true
}

private val CallableId.javaStaticMethodName: String
    get() = asSingleFqName().asString()
