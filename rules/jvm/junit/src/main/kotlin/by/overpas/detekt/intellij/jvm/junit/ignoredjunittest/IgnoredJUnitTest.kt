package by.overpas.detekt.intellij.jvm.junit.ignoredjunittest

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotation
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

@IntellijInspection("IgnoredJUnitTest")
class IgnoredJUnitTest(config: Config) :
    Rule(
        config,
        "A test class or test method is disabled with `@Ignore` or `@Disabled`, so it does not run. " +
            "Fix and enable the test, or give the reason why it is disabled.",
    ),
    RequiresAnalysisApi {

    @Configuration("Do not report the disabled tests that give a reason.")
    private val isReasonAccepted: Boolean by config(true)

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        val annotations = if (klass.annotationEntries.isEmpty()) {
            emptyList()
        } else {
            analyze(klass) { klass.ignoreAnnotations() }
        }
        ignoredMessage(
            annotations,
        )?.let { report(Finding(Entity.atName(klass), "Test class '${klass.name.orEmpty()}' $it")) }
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val annotations = if (function.annotationEntries.isEmpty()) {
            emptyList()
        } else {
            analyze(function) { function.ignoreAnnotations() }
        }
        ignoredMessage(annotations)?.let {
            report(Finding(Entity.atName(function), "Test method '${function.name.orEmpty()}()' $it"))
        }
    }

    private fun ignoredMessage(annotations: List<KaAnnotation>): String? {
        val hasReason = annotations.any { annotation -> annotation.arguments.any { it.name.asString() == "value" } }
        val suffix = if (isReasonAccepted) " without reason" else ""
        return "is ignored/disabled$suffix".takeIf { annotations.isNotEmpty() && !(isReasonAccepted && hasReason) }
    }
}
