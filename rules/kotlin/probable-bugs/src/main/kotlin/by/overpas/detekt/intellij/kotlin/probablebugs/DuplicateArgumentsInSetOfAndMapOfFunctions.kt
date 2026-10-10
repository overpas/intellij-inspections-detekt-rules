package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

@IntellijInspection("DuplicateArgumentsInSetOfAndMapOfFunctions")
class DuplicateArgumentsInSetOfAndMapOfFunctions(config: Config) :
    Rule(
        config,
        "A set or map factory call has duplicate constant elements or keys, so only one of them takes effect. " +
            "Remove or change the duplicates.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = (expression.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (name !in DUPLICATE_ARGUMENTS_MAP_FUNCTIONS && name !in DUPLICATE_ARGUMENTS_SET_FUNCTIONS) return
        DuplicateArgumentsInSetOfAndMapOfFunctionsKeys(expression).duplicates.forEach { (key, arguments) ->
            val message = "Duplicate element in collection: '${key ?: "null"}'"
            arguments.forEach { argument -> report(Finding(Entity.from(argument), message)) }
        }
    }
}
