package by.overpas.detekt.intellij.kotlin.redundantconstructs.redundantmodalitymodifier

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

@IntellijInspection("RedundantModalityModifier")
class RedundantModalityModifier(config: Config) :
    Rule(
        config,
        "A modality modifier that matches the default modality of the declaration is redundant. Remove it.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "The annotations of the all-open compiler plugin, given as fully qualified names, " +
            "e.g. `org.springframework.stereotype.Component`. A `final` modifier in a class with one of them " +
            "or with an annotation that is annotated with one of them is not reported.",
    )
    private val allOpenAnnotations: List<String> by config(emptyList())

    private val allOpen by lazy { RedundantModalityModifierAllOpen(allOpenAnnotations.toSet()) }

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val owners = analyze(file) {
            file.collectDiagnostics(KaDiagnosticCheckerFilter.ONLY_EXTENDED_CHECKERS)
                .filterIsInstance<KaFirDiagnostic.RedundantModalityModifier>()
                .mapNotNull { diagnostic -> diagnostic.psi.takeUnless { allOpen.isFinalModifierKept(it) } }
        }
        owners.filterNot { it.hasModifier(KtTokens.ABSTRACT_KEYWORD) && it.isInInterface() }
            .mapNotNull { it.modifierList?.getModifier(KtTokens.MODALITY_MODIFIERS) }
            .forEach { report(Finding(Entity.from(it), "Redundant '${it.text}' modifier")) }
    }

    private fun KtModifierListOwner.isInInterface(): Boolean {
        val owner = this as? KtDeclaration
        val klass = owner as? KtClass ?: owner?.containingClassOrObject as? KtClass
        return klass?.isInterface() == true
    }
}
