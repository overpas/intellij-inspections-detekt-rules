package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class RedundantValueArgument(config: Config) :
    Rule(
        config,
        "An argument that is equal to the default value of its parameter is redundant. Remove the argument.",
    ),
    RequiresAnalysisApi {

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        val callElement = (argument.parent as? KtValueArgumentList)?.getStrictParentOfType<KtCallElement>() ?: return
        val parameterName = analyze(argument) {
            argument
                .takeIf { it.getArgumentExpression()?.evaluate() != null }
                ?.redundantDefaultParameterName(callElement)
        }
        if (parameterName != null) {
            val message = "Value argument matches the default value of parameter '$parameterName'"
            report(Finding(Entity.from(argument), message))
        }
    }
}
