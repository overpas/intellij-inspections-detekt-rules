package by.overpas.detekt.intellij.android.kotlin.illegalidentifier

import by.overpas.detekt.intellij.IntellijInspection
import com.intellij.psi.PsiElement
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtFile

@IntellijInspection("IllegalIdentifier")
class IllegalIdentifier(config: Config) :
    Rule(
        config,
        "A backticked identifier has characters that the dex format does not allow in a name. Android versions " +
            "before 11 cannot run such code. Rename the identifier.",
    ) {

    override fun visitKtFile(file: KtFile) {
        if (!file.name.endsWith(".kts") && !file.isInHostTestSourceSet()) super.visitKtFile(file)
    }

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element.isIllegalDalvikIdentifier()) {
            report(Finding(Entity.from(element), "Identifier not allowed in Android projects"))
        }
    }
}
