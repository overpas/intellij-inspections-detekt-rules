package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtWhenExpression

@IntellijInspection("IntroduceWhenSubject")
class IntroduceWhenSubject(config: Config) :
    Rule(
        config,
        "Every branch of a `when` without a subject checks the same value. Move this value to the `when` subject.",
    ),
    RequiresAnalysisApi {

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)
        if (expression.subjectExpression != null || expression.entries.count { !it.isElse } < 2) return
        val subject = analyze(expression) { expression.whenSubjectToIntroduce() } ?: return
        report(Finding(Entity.from(expression.whenKeyword), "Introduce '${subject.text}' as subject of 'when'"))
    }
}
