package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtTypeArgumentList

class RemoveExplicitTypeArguments(config: Config) :
    Rule(
        config,
        "Explicit type arguments that the compiler can infer are redundant. Remove them.",
    ),
    RequiresAnalysisApi {

    override fun visitTypeArgumentList(typeArgumentList: KtTypeArgumentList) {
        super.visitTypeArgumentList(typeArgumentList)
        val call = typeArgumentList.parent as? KtCallExpression ?: return
        if (RemoveExplicitTypeArgumentsCall(call).isRedundant()) {
            report(Finding(Entity.from(typeArgumentList), "Explicit type arguments can be inferred"))
        }
    }
}
