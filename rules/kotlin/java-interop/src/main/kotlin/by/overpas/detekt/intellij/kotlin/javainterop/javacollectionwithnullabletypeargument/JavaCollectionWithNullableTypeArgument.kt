package by.overpas.detekt.intellij.kotlin.javainterop.javacollectionwithnullabletypeargument

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtTypeReference

@IntellijInspection("JavaCollectionWithNullableTypeArgument")
class JavaCollectionWithNullableTypeArgument(config: Config) :
    Rule(
        config,
        "This Java collection does not support `null` elements, keys or values. Use non-nullable type arguments.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "More collections that do not support `null`, given as fully qualified names, " +
            "e.g. `com.google.common.collect.ImmutableList`.",
    )
    private val additionalCollections: List<String> by config(emptyList())

    private val collections by lazy { JAVA_COLLECTIONS_WITHOUT_NULL_SUPPORT + additionalCollections }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val usage = analyze(expression) { expression.nullableJavaCollectionCall(collections) } ?: return
        report(Finding(Entity.from(expression), usage.message))
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {
        super.visitTypeReference(typeReference)
        val usage = analyze(typeReference) { typeReference.nullableJavaCollectionType(collections) } ?: return
        report(Finding(Entity.from(typeReference), usage.message))
    }
}
