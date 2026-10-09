package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

class ReplaceWithImportAlias(config: Config) :
    Rule(
        config,
        "A qualified name refers to a declaration that the file already imports under an alias. " +
            "Use the import alias instead.",
    ),
    RequiresAnalysisApi {

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        super.visitSimpleNameExpression(expression)
        val reference = expression as? KtNameReferenceExpression ?: return
        val aliases = ReplaceWithImportAliasImports(reference)
        if (aliases.isCandidate() && aliases.matchTarget()) {
            report(Finding(Entity.from(reference), "Replace with import alias"))
        }
    }
}
