package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression

private val enumValuesTopLevelFunctionId = CallableId(FqName("kotlin"), Name.identifier("enumValues"))

context(session: KaSession)
internal fun KtCallExpression.callsEnumValuesOfEnumClass(): Boolean =
    with(session) {
        resolveToCall()?.successfulFunctionCallOrNull()?.run {
            symbol.callableId == enumValuesTopLevelFunctionId &&
                typeArgumentsMapping.values.firstOrNull()?.expandedSymbol != null
        } == true
    }
