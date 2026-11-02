package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

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

private val DEFAULT_BLOCKING_ANNOTATIONS = listOf(
    "org.jetbrains.annotations.Blocking",
    "io.micronaut.core.annotation.Blocking",
    "io.smallrye.common.annotation.Blocking",
)

private val DEFAULT_NON_BLOCKING_ANNOTATIONS = listOf(
    "org.jetbrains.annotations.NonBlocking",
    "io.micronaut.core.annotation.NonBlocking",
    "io.smallrye.common.annotation.NonBlocking",
)

private val DEFAULT_BLOCKING_LAMBDA_FUNCTIONS = listOf(
    "kotlinx.coroutines.runBlocking",
    "org.mockito.kotlin.KStubbing.on",
    "org.mockito.kotlin.KStubbing.onBlocking",
    "org.mockito.kotlin.verifyBlocking",
    "io.mockk.coEvery",
    "io.mockk.coVerify",
    "dev.mokkery.everySuspend",
    "dev.mokkery.verifySuspend",
)

@IntellijInspection("BlockingMethodInNonBlockingContext")
class BlockingMethodInNonBlockingContext(config: Config) :
    Rule(
        config,
        "A possibly blocking call, such as `Thread.sleep()` or a method annotated with `@Blocking`, is in a " +
            "non-blocking context, such as a suspend function or a method annotated with `@NonBlocking`. " +
            "It can lead to thread starvation. Move the call to a blocking context, e.g. `Dispatchers.IO`.",
    ),
    RequiresAnalysisApi {

    @Configuration("Do not report the calls in a context that is not known as blocking or non-blocking.")
    private val isUnknownContextBlocking: Boolean by config(true)

    @Configuration("Consider Kotlin suspend functions and suspend lambdas non-blocking.")
    private val isSuspendContextNonBlocking: Boolean by config(true)

    @Configuration("The annotations of blocking methods and blocking classes.")
    private val blockingAnnotations: List<String> by config(DEFAULT_BLOCKING_ANNOTATIONS)

    @Configuration("The annotations of non-blocking methods and non-blocking classes.")
    private val nonBlockingAnnotations: List<String> by config(DEFAULT_NON_BLOCKING_ANNOTATIONS)

    @Configuration(
        "Functions whose lambda arguments run in a blocking context, given as fully qualified names, " +
            "e.g. `kotlinx.coroutines.runBlocking`.",
    )
    private val blockingLambdaFunctions: List<String> by config(DEFAULT_BLOCKING_LAMBDA_FUNCTIONS)

    private val annotations by lazy {
        BlockingMethodInNonBlockingContextAnnotations(
            (blockingAnnotations + DEFAULT_BLOCKING_ANNOTATIONS).toSet(),
            (nonBlockingAnnotations + DEFAULT_NON_BLOCKING_ANNOTATIONS).toSet(),
        )
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        val isReported = analyze(expression) {
            val context = BlockingMethodInNonBlockingContextContext(
                expression = expression,
                annotations = annotations,
                isSuspendContextNonBlocking = isSuspendContextNonBlocking,
                blockingLambdaFunctions = blockingLambdaFunctions,
            ).type()
            val isReportedContext = context == BlockingMethodInNonBlockingContextType.NON_BLOCKING ||
                (context == BlockingMethodInNonBlockingContextType.UNSURE && !isUnknownContextBlocking)
            isReportedContext && BlockingMethodInNonBlockingContextMethod(annotations).isBlocking(expression)
        }
        if (isReported) {
            report(
                Finding(
                    Entity.from(callee),
                    "Possibly blocking call in non-blocking context could lead to thread starvation",
                ),
            )
        }
    }
}
