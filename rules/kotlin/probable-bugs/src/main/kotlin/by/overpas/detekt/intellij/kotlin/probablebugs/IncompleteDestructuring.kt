package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration

@IntellijInspection("IncompleteDestructuring")
class IncompleteDestructuring(config: Config) :
    Rule(
        config,
        "A destructuring declaration of a data class omits some of its components. " +
            "Add the missing components and use `_` for the unused ones.",
    ),
    RequiresAnalysisApi {

    override fun visitDestructuringDeclaration(multiDeclaration: KtDestructuringDeclaration) {
        super.visitDestructuringDeclaration(multiDeclaration)
        val isIncomplete = analyze(multiDeclaration) { multiDeclaration.isIncompleteDestructuring() }
        if (isIncomplete) report(Finding(Entity.from(multiDeclaration), "Incomplete destructuring declaration"))
    }
}
