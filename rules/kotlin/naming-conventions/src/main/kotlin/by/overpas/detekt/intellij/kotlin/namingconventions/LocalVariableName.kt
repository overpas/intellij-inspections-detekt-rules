package by.overpas.detekt.intellij.kotlin.namingconventions

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDestructuringDeclarationEntry
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("LocalVariableName")
class LocalVariableName(config: Config) :
    Rule(
        config,
        "A local variable or parameter name does not follow the Kotlin naming convention. " +
            "Rename it to lowerCamelCase with only letters and digits.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (property.isLocal) property.reportNameMismatch()
    }

    override fun visitParameter(parameter: KtParameter) {
        super.visitParameter(parameter)
        if (!parameter.hasValOrVar()) parameter.reportNameMismatch()
    }

    override fun visitDestructuringDeclarationEntry(multiDeclarationEntry: KtDestructuringDeclarationEntry) {
        super.visitDestructuringDeclarationEntry(multiDeclarationEntry)
        multiDeclarationEntry.reportNameMismatch()
    }

    private fun KtNamedDeclaration.reportNameMismatch() {
        val identifier = nameIdentifier ?: return
        name?.localVariableNameMismatch()?.let { mismatch ->
            report(Finding(Entity.from(identifier), "Local variable name ${identifier.text} $mismatch"))
        }
    }
}
