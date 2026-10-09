package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotationValue
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.types.Variance

private const val MAIN_FUNCTION_RETURN_UNIT_NAME = "main"

private val MAIN_FUNCTION_RETURN_UNIT_JVM_NAME = ClassId.topLevel(FqName("kotlin.jvm.JvmName"))

private val MAIN_FUNCTION_RETURN_UNIT_JVM_STATIC = ClassId.topLevel(FqName("kotlin.jvm.JvmStatic"))

internal class MainFunctionReturnUnitCandidate(private val function: KtNamedFunction) {

    private val parameterCount: Int
        get() = function.valueParameters.size + if (function.receiverTypeReference == null) 0 else 1

    private val KaType.isResolvedClassType: Boolean
        get() = generateSequence(listOf(this)) { types ->
            types.flatMap { type -> (type as? KaClassType)?.typeArguments.orEmpty().mapNotNull { it.type } }
        }.takeWhile { it.isNotEmpty() }.flatten().all { it is KaClassType }

    private val hasMainShape: Boolean
        get() = !function.isLocal &&
            function.typeParameters.isEmpty() &&
            (parameterCount == 1 || (parameterCount == 0 && function.isTopLevel))

    @OptIn(KaExperimentalApi::class)
    private val hasMainParameterType: Boolean
        get() {
            val reference = function.receiverTypeReference ?: function.valueParameters.singleOrNull()?.typeReference
            return reference == null ||
                analyze(reference) {
                    val mainParameterType = typeCreator.arrayType(typeCreator.classType(StandardClassIds.String)) {
                        variance = Variance.OUT_VARIANCE
                        isMarkedNullable = true
                    }
                    reference.type.run { isResolvedClassType && isSubtypeOf(mainParameterType) }
                }
        }

    private val jvmName: String?
        get() = analyze(function) {
            val annotation = function.symbol.annotations[MAIN_FUNCTION_RETURN_UNIT_JVM_NAME].firstOrNull()
            val argument = annotation?.run { arguments.firstOrNull()?.expression } as? KaAnnotationValue.ConstantValue
            argument?.run { value.value } as? String
        }

    private val hasMainName: Boolean
        get() = (jvmName ?: function.name) == MAIN_FUNCTION_RETURN_UNIT_NAME &&
            (parameterCount == 1 || function.name == MAIN_FUNCTION_RETURN_UNIT_NAME)

    private val hasMainOwner: Boolean
        get() = function.isTopLevel ||
            analyze(function) {
                val symbol = function.symbol
                val owner = symbol.containingSymbol as? KaClassSymbol
                owner?.run { classKind.isObject } == true && MAIN_FUNCTION_RETURN_UNIT_JVM_STATIC in symbol.annotations
            }

    val hasParameter: Boolean
        get() = parameterCount == 1

    val isEntryPoint: Boolean
        get() = hasMainShape && hasMainParameterType && hasMainName && hasMainOwner
}
