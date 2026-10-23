package by.overpas.detekt.intellij.jvm.junit.ignoredjunittest

import by.overpas.detekt.intellij.jvm.junit.JUPITER_DISABLED
import by.overpas.detekt.intellij.jvm.junit.ORG_JUNIT_IGNORE
import by.overpas.detekt.intellij.jvm.junit.findAnnotations
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotation
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtDeclaration

context(session: KaSession)
internal fun KtDeclaration.ignoreAnnotations(): List<KaAnnotation> =
    findAnnotations(ORG_JUNIT_IGNORE, JUPITER_DISABLED)
        .filterNot { (it.psi as? KtAnnotationEntry)?.isPlatformAlias() == true }

context(session: KaSession)
private fun KtAnnotationEntry.isPlatformAlias(): Boolean =
    with(session) {
        val alias = typeReference?.type?.abbreviation?.symbol
        alias != null && (alias.isActual || alias.origin != KaSymbolOrigin.SOURCE)
    }
