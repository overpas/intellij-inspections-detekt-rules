package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("UnnecessaryVariable")
class UnnecessaryVariable(config: Config) :
    Rule(
        config,
        "A local variable that only copies another value is unnecessary. Use the original value instead.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val initializer = property.initializer as? KtNameReferenceExpression ?: return
        val isCopy = property.isLocal &&
            analyze(property) {
                val copy = UnnecessaryVariableCopy(this, property, initializer)
                copy.isPlainDeclaration() && copy.hasCopyableSource() && copy.isUsed() && !copy.hasNameConflict()
            }
        if (isCopy) {
            report(
                Finding(
                    Entity.from(property.nameIdentifier ?: property),
                    "Variable is same as '${initializer.getReferencedName()}' and can be inlined",
                ),
            )
        }
    }
}
