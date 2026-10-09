package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression

private val replaceCollectionCountWithSizeCallableId =
    CallableId(FqName("kotlin.collections"), Name.identifier("count"))

private val replaceCollectionCountWithSizeReceivers =
    buildSet {
        add(StandardClassIds.Collection)
        add(StandardClassIds.Array)
        add(StandardClassIds.Map)
        addAll(StandardClassIds.elementTypeByPrimitiveArrayType.keys)
        addAll(StandardClassIds.unsignedArrayTypeByElementType.keys)
    }

class ReplaceCollectionCountWithSize(config: Config) :
    Rule(
        config,
        "`count()` without a predicate on a collection, an array or a map returns its size. " +
            "Use the `size` property instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        val isCandidate = callee.text == "count" && expression.valueArguments.isEmpty()
        if (isCandidate && expression.isCollectionCount()) {
            report(Finding(Entity.from(callee), "Collection count can be converted to size"))
        }
    }

    private fun KtCallExpression.isCollectionCount(): Boolean =
        analyze(this) {
            val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            val receiverClassId = (symbol?.receiverParameter?.returnType as? KaClassType)?.classId
            symbol?.callableId == replaceCollectionCountWithSizeCallableId &&
                receiverClassId in replaceCollectionCountWithSizeReceivers
        }
}
