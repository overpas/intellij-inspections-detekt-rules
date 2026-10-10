package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtObjectDeclaration

@IntellijInspection("JavaIoSerializableObjectMustHaveReadResolve")
class JavaIoSerializableObjectMustHaveReadResolve(config: Config) :
    Rule(
        config,
        "Java deserialization of an object that implements `java.io.Serializable` creates a new instance. " +
            "Add `private fun readResolve(): Any` that returns the object.",
    ),
    RequiresAnalysisApi {

    override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
        super.visitObjectDeclaration(declaration)
        val name = declaration.nameIdentifier?.takeUnless { declaration.isObjectLiteral() } ?: return
        val isMissingReadResolve = analyze(declaration) { declaration.isMissingReadResolve() }
        if (isMissingReadResolve) report(Finding(Entity.from(name), "Serializable object must implement 'readResolve'"))
    }
}
