package by.overpas.detekt.intellij.kotlin.javainterop

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression

private val javaCollectionsClassId = ClassId.fromString("java/util/Collections")

context(session: KaSession)
internal fun KtCallExpression.mutatorOnImmutableList(): String? =
    with(session) {
        val argumentType = valueArguments.firstOrNull()?.getArgumentExpression()?.expressionType
        val isImmutableList = argumentType != null &&
            argumentType.isSubtypeOf(StandardClassIds.List) &&
            !argumentType.isSubtypeOf(StandardClassIds.MutableList)
        val callableId = resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
        val methodName = callableId?.takeIf { it.classId == javaCollectionsClassId }?.run { callableName.asString() }
        methodName?.takeIf { isImmutableList && hasMutatorArguments(it) }
    }
