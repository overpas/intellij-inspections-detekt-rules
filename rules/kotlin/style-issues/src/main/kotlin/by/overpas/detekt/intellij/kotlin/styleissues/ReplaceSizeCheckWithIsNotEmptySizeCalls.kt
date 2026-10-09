package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.analysis.api.resolution.singleCallOrNull
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

private val sizeCheckCountName = Name.identifier("count")

private val sizeCheckCharSequenceReceivers: Set<ClassId> = setOf(StandardClassIds.CharSequence)

private val sizeCheckSizeReceivers: Set<ClassId> =
    buildSet {
        add(StandardClassIds.Collection)
        add(StandardClassIds.Array)
        add(StandardClassIds.Map)
        addAll(StandardClassIds.primitiveArrayTypeByElementType.values)
        addAll(StandardClassIds.unsignedArrayTypeByElementType.values)
    }

private val sizeCheckProgressionReceivers: Set<ClassId> =
    listOf("Char", "Int", "UInt", "Long", "ULong")
        .map { type -> ClassId(StandardClassIds.BASE_RANGES_PACKAGE, Name.identifier("${type}Progression")) }
        .toSet()

private val sizeCheckReceiversByCallableId: Map<CallableId, Set<ClassId>> =
    sizeCheckSizeReceivers.associateBy({ CallableId(it, Name.identifier("size")) }, { setOf(it) }) +
        mapOf(
            CallableId(StandardClassIds.CharSequence, Name.identifier("length")) to sizeCheckCharSequenceReceivers,
            CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, sizeCheckCountName) to
                sizeCheckSizeReceivers + sizeCheckProgressionReceivers,
            CallableId(StandardClassIds.BASE_TEXT_PACKAGE, sizeCheckCountName) to sizeCheckCharSequenceReceivers,
        )

internal fun KtExpression?.isSizeCheckConstant(value: Int): Boolean {
    val constant = this?.let(KtPsiUtil::deparenthesize) as? KtConstantExpression
    return constant?.run { node.elementType == KtNodeTypes.INTEGER_CONSTANT && text.toIntOrNull() == value } == true
}

internal fun KtExpression.isInsideSizeCheckReplacement(name: String): Boolean =
    getStrictParentOfType<KtFunction>()?.takeIf { it.valueParameters.isEmpty() }?.name == name

@OptIn(KaExperimentalApi::class)
internal fun KtExpression.isReplaceableSizeCheckCall(): Boolean =
    this !is KtSafeQualifiedExpression &&
        analyze(this) {
            val call = resolveToCall()?.singleCallOrNull<KaCallableMemberCall<*, *>>() as? KaSingleCall<*, *>
            val symbols = call?.let { sizeCall -> listOf(sizeCall.signature.symbol) }.orEmpty()
            val callableIds = (symbols + symbols.flatMap { symbol -> symbol.allOverriddenSymbols })
                .mapNotNull { symbol -> symbol.callableId }
            val receiverTypes = listOfNotNull((call?.extensionReceiver ?: call?.dispatchReceiver)?.type)
            val receiverClassIds = (receiverTypes + receiverTypes.flatMap { type -> type.allSupertypes })
                .mapNotNull { type -> type.expandedSymbol?.classId }
            val hasNoArguments = (call as? KaFunctionCall<*>)?.valueArgumentMapping.isNullOrEmpty()
            hasNoArguments &&
                callableIds.any { id -> sizeCheckReceiversByCallableId[id].orEmpty().any { it in receiverClassIds } }
        }
