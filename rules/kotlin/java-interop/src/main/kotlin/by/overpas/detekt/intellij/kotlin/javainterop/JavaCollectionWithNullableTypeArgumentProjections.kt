package by.overpas.detekt.intellij.kotlin.javainterop

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassifierSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeProjection
import org.jetbrains.kotlin.psi.KtUserType

context(session: KaSession)
internal fun List<KtTypeProjection>.nullableTypeArgumentCount(): Int =
    count { it.typeReference?.typeElement is KtNullableType || it.isImplicitlyNullable() }

context(session: KaSession)
private fun KtTypeProjection.isImplicitlyNullable(): Boolean =
    with(session) {
        val referenceExpression = (typeReference?.typeElement as? KtUserType)?.referenceExpression
        val symbol = referenceExpression?.run {
            references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        }
        (symbol as? KaClassifierSymbol)?.run { defaultType.isNullable } == true
    }
