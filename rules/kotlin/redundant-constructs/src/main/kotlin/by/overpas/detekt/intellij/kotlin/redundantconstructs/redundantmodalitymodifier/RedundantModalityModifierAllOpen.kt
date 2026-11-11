package by.overpas.detekt.intellij.kotlin.redundantconstructs.redundantmodalitymodifier

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

internal class RedundantModalityModifierAllOpen(private val annotations: Set<String>) {

    context(session: KaSession)
    fun isFinalModifierKept(owner: KtModifierListOwner): Boolean {
        val klass = owner as? KtClassOrObject ?: (owner as? KtDeclaration)?.containingClassOrObject
        return annotations.isNotEmpty() &&
            owner.hasModifier(KtTokens.FINAL_KEYWORD) &&
            klass != null &&
            with(session) { klass.symbol.annotations.any { isAllOpen(it.classId, mutableSetOf()) } }
    }

    context(session: KaSession)
    private fun isAllOpen(
        classId: ClassId?,
        visited: MutableSet<ClassId>,
    ): Boolean =
        when {
            classId == null || !visited.add(classId) -> false

            classId.asSingleFqName().asString() in annotations -> true

            else -> with(session) { findClass(classId)?.annotations }
                .orEmpty()
                .any { isAllOpen(it.classId, visited) }
        }
}
