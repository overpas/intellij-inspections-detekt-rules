package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

class ConvertReferenceToLambda(config: Config) :
    Rule(
        config,
        "A callable reference can be replaced with a lambda. " +
            "Use a lambda where it reads better than the reference.",
    ),
    RequiresAnalysisApi {

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        val isConvertible = analyze(expression) {
            val expectedPackage = (expression.expectedType as? KaClassType)?.run { classId.packageFqName }
            val isResolved = expression.callableReference.references
                .filterIsInstance<KtReference>()
                .any { it.resolveToSymbol() != null }
            isResolved && expectedPackage != StandardNames.KOTLIN_REFLECT_FQ_NAME
        }
        if (isConvertible) report(Finding(Entity.from(expression), "Reference can be converted to a lambda"))
    }
}
