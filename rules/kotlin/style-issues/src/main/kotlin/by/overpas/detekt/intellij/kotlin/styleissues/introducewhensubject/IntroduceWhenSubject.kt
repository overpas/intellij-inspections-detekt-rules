package by.overpas.detekt.intellij.kotlin.styleissues.introducewhensubject

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtWhenConditionWithExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression

@IntellijInspection("IntroduceWhenSubject")
class IntroduceWhenSubject(config: Config) :
    Rule(
        config,
        "Every branch of a `when` without a subject checks the same value. Move this value to the `when` subject.",
    ),
    RequiresAnalysisApi {

    @Configuration("Do not report a `when` with a branch condition joined by `&&`, which becomes a guard.")
    private val areWhenGuardsIgnored: Boolean by config(true)

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)
        val entries = expression.entries.filterNot { it.isElse }
        val isGuarded = areWhenGuardsIgnored && entries.any { it.hasConjunction() }
        if (expression.subjectExpression != null || entries.size < 2 || isGuarded) return
        val subject = analyze(expression) { expression.whenSubjectToIntroduce() } ?: return
        report(Finding(Entity.from(expression.whenKeyword), "Introduce '${subject.text}' as subject of 'when'"))
    }

    private fun KtWhenEntry.hasConjunction(): Boolean =
        conditions.any { condition ->
            val expression = (condition as? KtWhenConditionWithExpression)?.expression as? KtBinaryExpression
            expression?.operationToken == KtTokens.ANDAND
        }
}
