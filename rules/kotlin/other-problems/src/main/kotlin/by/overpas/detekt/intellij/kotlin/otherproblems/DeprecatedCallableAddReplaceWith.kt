package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class DeprecatedCallableAddReplaceWith(config: Config) :
    Rule(
        config,
        "A `@Deprecated` callable with a simple body can tell its callers how to migrate. " +
            "Add a `replaceWith` argument with the body as the replacement.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val annotation = function.deprecatedAnnotationOrNull() ?: return
        val isMissingReplaceWith = analyze(function) { function.canSuggestReplaceWith() }
        if (isMissingReplaceWith) report(Finding(Entity.from(annotation), MESSAGE))
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val annotation = property.takeUnless { it.isVar }?.deprecatedAnnotationOrNull() ?: return
        val isMissingReplaceWith = analyze(property) { property.canSuggestReplaceWith() }
        if (isMissingReplaceWith) report(Finding(Entity.from(annotation), MESSAGE))
    }

    private companion object {
        const val MESSAGE = "'@Deprecated' annotation without a 'replaceWith' argument"
    }
}
