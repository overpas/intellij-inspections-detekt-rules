package by.overpas.detekt.intellij.kotlin.javainterop

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

context(session: KaSession)
internal fun KtCallExpression.nullableJavaCollectionCall(): JavaCollectionWithNullableTypeArgumentUsage? =
    with(session) {
        val call = resolveToCall()?.successfulFunctionCallOrNull()
        val classId = (call?.symbol as? KaConstructorSymbol)?.containingClassId
        val nullableCount = typeArguments.nullableTypeArgumentCount()
        val hasInferredNullableTypes = typeArguments.isEmpty() &&
            call?.run { typeArgumentsMapping.values.any { it.isMarkedNullable } } == true
        classId
            ?.takeIf { nullableCount > 0 || hasInferredNullableTypes }
            ?.toNullableJavaCollectionUsage(nullableCount)
    }

context(session: KaSession)
internal fun KtTypeReference.nullableJavaCollectionType(): JavaCollectionWithNullableTypeArgumentUsage? =
    with(session) {
        val userType = typeElement?.unwrapNullability() as? KtUserType
        val nullableCount = userType?.typeArguments.orEmpty().nullableTypeArgumentCount()
        val classId = type.symbol?.classId
        classId?.takeIf { nullableCount > 0 }?.toNullableJavaCollectionUsage(nullableCount)
    }

private fun KtTypeElement.unwrapNullability(): KtTypeElement? =
    if (this is KtNullableType) innerType?.unwrapNullability() else this
