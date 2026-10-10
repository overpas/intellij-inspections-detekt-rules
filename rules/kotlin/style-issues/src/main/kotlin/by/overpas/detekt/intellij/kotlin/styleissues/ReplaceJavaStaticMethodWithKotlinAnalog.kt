package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("ReplaceJavaStaticMethodWithKotlinAnalog")
class ReplaceJavaStaticMethodWithKotlinAnalog(config: Config) :
    Rule(
        config,
        "The called Java static method has a counterpart in the Kotlin standard library. " +
            "Call the Kotlin function instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (ReplaceJavaStaticMethodWithKotlinAnalogCall(expression).isReplaceable()) {
            report(Finding(Entity.from(expression), "Should be replaced with Kotlin function"))
        }
    }
}
