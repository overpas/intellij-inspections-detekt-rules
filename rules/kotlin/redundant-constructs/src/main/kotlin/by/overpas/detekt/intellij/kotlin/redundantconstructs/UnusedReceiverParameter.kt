package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

@IntellijInspection("UnusedReceiverParameter")
class UnusedReceiverParameter(config: Config) :
    Rule(
        config,
        "The receiver of an extension is never used. Remove the receiver or use a plain function or property.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.hasBody() && !function.isCalledWithExplicitReceiver()) function.reportUnusedReceiver()
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (property.accessors.isNotEmpty() && property.delegate == null) property.reportUnusedReceiver()
    }

    private fun KtCallableDeclaration.reportUnusedReceiver() {
        val receiver = receiverTypeReference ?: return
        val declaration = this
        val isUnused = analyze(declaration) {
            val usage = UnusedReceiverParameterUsage(this, declaration, receiver)
            !usage.hasExcludedModifier() &&
                !usage.isExempt() &&
                if (usage.usesClassLabels()) !usage.hasReceiverLabel() else !usage.isReceiverUsed()
        }
        if (isUnused) report(Finding(Entity.from(receiver), "Receiver parameter is never used"))
    }

    private fun KtNamedFunction.isCalledWithExplicitReceiver(): Boolean {
        val call = getStrictParentOfType<KtQualifiedExpression>()?.selectorExpression as? KtCallExpression
        return name == null && call?.calleeExpression?.let { KtPsiUtil.deparenthesize(it) } == this
    }
}
