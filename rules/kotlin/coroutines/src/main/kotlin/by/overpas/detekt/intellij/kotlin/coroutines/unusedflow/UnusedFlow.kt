package by.overpas.detekt.intellij.kotlin.coroutines.unusedflow

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression

@IntellijInspection("UnusedFlow")
class UnusedFlow(config: Config) :
    Rule(
        config,
        "A `Flow` does nothing until it is collected, so an unused `Flow` value has no effect. Collect the flow, " +
            "pass it on or remove it.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Functions whose lambda arguments are not checked, given as fully qualified names, e.g. `io.mockk.verify`.",
    )
    private val ignoredLambdaFunctions: Set<String> by config(UNUSED_FLOW_IGNORED_LAMBDA_FUNCTIONS) { it.toSet() }

    override fun visitExpression(expression: KtExpression) {
        super.visitExpression(expression)
        if (!expression.isUnusedFlowCandidate()) return
        val isUnused = analyze(expression) {
            val call = expression.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()
            (call?.run { symbol.returnType } as? KaClassType)?.classId == FLOW &&
                !expression.isUsedAsExpression &&
                !expression.isInIgnoredLambda(ignoredLambdaFunctions)
        }
        if (isUnused) report(Finding(Entity.from(expression), "Flow is constructed but not used"))
    }

    private companion object {
        val FLOW = ClassId(FqName("kotlinx.coroutines.flow"), Name.identifier("Flow"))
    }
}
