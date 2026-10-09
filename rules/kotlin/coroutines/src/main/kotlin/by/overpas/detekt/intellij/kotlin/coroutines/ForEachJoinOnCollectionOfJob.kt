package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression

private val FOR_EACH_ID = CallableId(FqName("kotlin.collections"), Name.identifier("forEach"))

private val JOIN_ID = CallableId(ClassId(FqName("kotlinx.coroutines"), Name.identifier("Job")), Name.identifier("join"))

class ForEachJoinOnCollectionOfJob(config: Config) :
    Rule(
        config,
        "Joining each job of a collection in `forEach { it.join() }` is verbose. Call `joinAll()` on the collection.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isReported = analyze(expression) {
            ForEachJoinOnCollectionOfJobCall(this, expression, FOR_EACH_ID)
                .collectionLambda()
                ?.returnsCallOnParameter(JOIN_ID) == true
        }
        if (isReported) {
            report(
                Finding(
                    Entity.from(expression.calleeExpression ?: expression),
                    "Usage of 'forEach { it.join() }' on 'Collection<Job>' instead of single 'joinAll()'",
                ),
            )
        }
    }
}
