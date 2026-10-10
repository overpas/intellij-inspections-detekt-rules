package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("JoinDeclarationAndAssignment")
class JoinDeclarationAndAssignment(config: Config) :
    Rule(
        config,
        "A property is declared without an initializer and assigned later. Join the declaration and the assignment.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val assignment = property.joinableAssignment() ?: return
        if (analyze(property) { JoinDeclarationAndAssignmentCheck(this, property, assignment).isJoinable() }) {
            report(Finding(Entity.from(property), "Can be joined with assignment"))
        }
    }
}
