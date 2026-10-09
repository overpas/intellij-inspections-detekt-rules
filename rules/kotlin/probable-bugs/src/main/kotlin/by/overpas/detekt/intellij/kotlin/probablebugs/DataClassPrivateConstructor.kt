package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPrimaryConstructor

class DataClassPrivateConstructor(config: Config) :
    Rule(
        config,
        "The generated `copy()` method of a data class exposes its private primary constructor. " +
            "Annotate the class with `@ConsistentCopyVisibility` or make the constructor public.",
    ),
    RequiresAnalysisApi {

    override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor) {
        super.visitPrimaryConstructor(constructor)
        val keyword = constructor.modifierList?.getModifier(KtTokens.PRIVATE_KEYWORD) ?: return
        val containingClass = constructor.getContainingClassOrObject() as? KtClass ?: return
        if (containingClass.isDataClassPrivateConstructorExposed) {
            report(
                Finding(
                    Entity.from(keyword),
                    "Private primary constructor is exposed via the generated 'copy()' method of a 'data' class.",
                ),
            )
        }
    }
}
