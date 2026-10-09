package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtWhenConditionWithExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

context(session: KaSession)
internal fun KtWhenExpression.whenSubjectToIntroduce(): KtExpression? {
    val conditions = entries.flatMap { it.conditions.asList() }
    val isApplicable = entries.all { it.isElse || it.conditions.isNotEmpty() } &&
        conditions.all { it is KtWhenConditionWithExpression }
    val areGuardsSupported = (session.useSiteModule as? KaSourceModule)
        ?.run { languageVersionSettings.supportsFeature(LanguageFeature.WhenGuards) } != false
    val initialState = if (areGuardsSupported) {
        IntroduceWhenSubjectState.UNCONSTRAINED
    } else {
        IntroduceWhenSubjectState.GUARDS_NOT_SUPPORTED
    }
    val search = IntroduceWhenSubjectSearch(session, initialState)
    val expressions = conditions.mapNotNull { (it as? KtWhenConditionWithExpression)?.expression }
    val candidates = expressions.map { expression ->
        search.candidateOf(expression)?.takeIf { it.hasWhenSubjectShape() }
    }
    val subject = candidates.lastOrNull()
    val isMatching = subject != null && candidates.all { it.matchesWhenSubject(subject) }
    val isEvaluatedOnce = subject?.mayInvokeWhenSubjectGetter() != true || subject.evaluationsIn(expressions) <= 1
    return subject.takeIf { isApplicable && isMatching && isEvaluatedOnce }
}

context(session: KaSession)
private fun KtExpression.evaluationsIn(conditions: List<KtExpression>): Int =
    conditions.sumOf { condition ->
        condition.collectDescendantsOfType<KtExpression>().count { it.matchesWhenSubject(this) }
    }
