package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression

class SuspiciousMutableCollectionInStateFlow(config: Config) :
    Rule(
        config,
        "A `MutableStateFlow` emits no new value when its mutable collection is changed in place. Store a " +
            "read-only collection and replace it on each change.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val argument = expression.valueArguments.firstOrNull() ?: return
        if (argument.getArgumentExpression() == null || !expression.isStateFlowFactoryCall()) return
        val isReported = analyze(expression) {
            val flowType = expression.expressionType as? KaClassType
            val valueType = flowType?.takeIf { it.classId == MUTABLE_STATE_FLOW }
                ?.typeArguments
                ?.singleOrNull()
                ?.type as? KaClassType
            valueType != null &&
                (sequenceOf(valueType) + valueType.allSupertypes).any {
                    it is KaClassType && it.classId in MUTABLE_TYPES
                }
        }
        if (isReported) {
            report(
                Finding(
                    Entity.from(argument),
                    "'MutableStateFlow' emits no new value after a mutation of this collection",
                ),
            )
        }
    }

    private fun KtCallExpression.isStateFlowFactoryCall(): Boolean =
        calleeExpression?.text?.let { name ->
            name == MUTABLE_STATE_FLOW.shortClassName.asString() ||
                containingKtFile.importDirectives.any {
                    it.importedFqName == MUTABLE_STATE_FLOW.asSingleFqName() && it.aliasName == name
                }
        } == true

    private companion object {
        val MUTABLE_STATE_FLOW = ClassId(FqName("kotlinx.coroutines.flow"), Name.identifier("MutableStateFlow"))
        val MUTABLE_TYPES = setOf(StandardClassIds.MutableCollection, StandardClassIds.MutableMap)
    }
}
