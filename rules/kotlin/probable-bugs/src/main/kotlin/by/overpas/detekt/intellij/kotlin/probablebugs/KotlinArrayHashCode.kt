package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private val KOTLIN_ARRAY_HASH_CODE_CALLABLE_ID = CallableId(StandardClassIds.Any, Name.identifier("hashCode"))

class KotlinArrayHashCode(config: Config) :
    Rule(
        config,
        "`hashCode()` of an array depends on the array identity, not on its contents. " +
            "Use `contentHashCode()` or `contentDeepHashCode()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val call = expression.noArgumentCallNamed(KOTLIN_ARRAY_HASH_CODE_CALLABLE_ID.callableName) ?: return
        val isArrayHashCode = analyze(expression) { expression.isArrayMemberCall(KOTLIN_ARRAY_HASH_CODE_CALLABLE_ID) }
        if (isArrayHashCode) report(Finding(Entity.from(call), "'hashCode()' called on array"))
    }
}
