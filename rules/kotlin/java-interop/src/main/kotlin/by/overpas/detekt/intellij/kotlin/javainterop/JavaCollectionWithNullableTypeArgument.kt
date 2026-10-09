package by.overpas.detekt.intellij.kotlin.javainterop

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtTypeReference

class JavaCollectionWithNullableTypeArgument(config: Config) :
    Rule(
        config,
        "This Java collection does not support `null` elements, keys or values. Use non-nullable type arguments.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val usage = analyze(expression) { expression.nullableJavaCollectionCall() } ?: return
        report(Finding(Entity.from(expression), usage.message))
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {
        super.visitTypeReference(typeReference)
        val usage = analyze(typeReference) { typeReference.nullableJavaCollectionType() } ?: return
        report(Finding(Entity.from(typeReference), usage.message))
    }
}
