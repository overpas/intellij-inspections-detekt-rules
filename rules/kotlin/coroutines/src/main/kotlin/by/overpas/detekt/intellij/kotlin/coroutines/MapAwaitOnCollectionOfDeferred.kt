package by.overpas.detekt.intellij.kotlin.coroutines

import by.overpas.detekt.intellij.IntellijInspection
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

private val MAP_ID = CallableId(FqName("kotlin.collections"), Name.identifier("map"))

private val AWAIT_ID = CallableId(
    ClassId(FqName("kotlinx.coroutines"), Name.identifier("Deferred")),
    Name.identifier("await"),
)

@IntellijInspection("MapAwaitOnCollectionOfDeferred")
class MapAwaitOnCollectionOfDeferred(config: Config) :
    Rule(
        config,
        "Awaiting each deferred of a collection in `map { it.await() }` is verbose. " +
            "Call `awaitAll()` on the collection.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isReported = analyze(expression) {
            ForEachJoinOnCollectionOfJobCall(this, expression, MAP_ID)
                .collectionLambda()
                ?.returnsCallOnParameter(AWAIT_ID) == true
        }
        if (isReported) {
            report(
                Finding(
                    Entity.from(expression.calleeExpression ?: expression),
                    "Usage of 'map { it.await() }' on 'Collection<Deferred>' instead of single 'awaitAll()'",
                ),
            )
        }
    }
}
