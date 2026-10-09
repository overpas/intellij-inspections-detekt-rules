package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtProperty

internal fun KtCallableDeclaration.hasImplicitNullableNothingType(): Boolean {
    val isVarOrOpen = (this as? KtProperty)?.isVar == true || hasModifier(KtTokens.OPEN_KEYWORD)
    val isCandidate = typeReference == null && isVarOrOpen && nameIdentifier != null
    val declaration = this
    return isCandidate && analyze(declaration) {
        (declaration.symbol as? KaCallableSymbol)?.run {
            val isOverridingNullableNothing = declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD) &&
                allOverriddenSymbols.any { it.returnType.isNullableNothing() }
            returnType.isNullableNothing() && !isOverridingNullableNothing
        } == true
    }
}

context(session: KaSession)
private fun KaType.isNullableNothing(): Boolean =
    (this as? KaClassType)?.classId == StandardClassIds.Nothing && with(session) { isMarkedNullable }
