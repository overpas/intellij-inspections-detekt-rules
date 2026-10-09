package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtAnnotationEntry

class RemoveEmptyParenthesesFromAnnotationEntry(config: Config) :
    Rule(
        config,
        "Empty parentheses after an annotation that needs no arguments are redundant. Remove them.",
    ),
    RequiresAnalysisApi {

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        super.visitAnnotationEntry(annotationEntry)
        val argumentList = annotationEntry.valueArgumentList
            ?.takeIf { it.arguments.isEmpty() && annotationEntry.typeArguments.isEmpty() }
            ?: return
        val typeReference = annotationEntry.typeReference ?: return
        val hasOptionalArguments = analyze(annotationEntry) {
            typeReference.type.expandedSymbol?.run {
                declaredMemberScope.constructors.any { constructor ->
                    constructor.valueParameters.all { it.hasDefaultValue }
                }
            } == true
        }
        if (hasOptionalArguments) report(Finding(Entity.from(argumentList), "Parentheses should be removed"))
    }
}
