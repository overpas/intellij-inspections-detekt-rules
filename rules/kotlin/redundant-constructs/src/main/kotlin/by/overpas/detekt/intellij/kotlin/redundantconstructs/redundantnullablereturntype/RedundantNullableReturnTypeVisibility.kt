package by.overpas.detekt.intellij.kotlin.redundantconstructs.redundantnullablereturntype

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

private val PUBLIC_API_VISIBILITIES = setOf(KtTokens.PUBLIC_KEYWORD, KtTokens.PROTECTED_KEYWORD)

internal fun KtDeclaration.isPublicApi(): Boolean =
    generateSequence(this) { it.containingClassOrObject }.all { declaration ->
        (declaration.parent is KtFile || declaration.parent is KtClassBody) &&
            declaration.visibilityModifierTypeOrDefault() in PUBLIC_API_VISIBILITIES
    }
