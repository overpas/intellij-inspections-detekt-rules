package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassLiteralExpression

internal val OPT_IN_CLASS_IDS = setOf(
    ClassId.topLevel(FqName("kotlin.OptIn")),
    ClassId.topLevel(FqName("kotlin.UseExperimental")),
)

internal class UnnecessaryOptInAnnotationMarkers(
    private val session: KaSession,
    private val entry: KtAnnotationEntry,
    private val owner: KtAnnotated,
) {

    fun isOptIn(): Boolean =
        with(session) { (entry.typeReference?.type as? KaClassType)?.classId in OPT_IN_CLASS_IDS }

    fun unusedMarkers(): List<Pair<KtClassLiteralExpression, ClassId>> {
        val found = UnnecessaryOptInAnnotationCollector(session).also { owner.accept(it) }.foundMarkers
        return entry.valueArguments
            .mapNotNull { (it.getArgumentExpression() as? KtClassLiteralExpression)?.marker() }
            .filter { it.second !in found }
    }

    private fun KtClassLiteralExpression.marker(): Pair<KtClassLiteralExpression, ClassId>? {
        val expression = this
        val argument = with(session) { (expressionType as? KaClassType)?.run { typeArguments.firstOrNull()?.type } }
        return (argument as? KaClassType)?.let { expression to it.classId }
    }
}
