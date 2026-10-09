package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtParameter

context(session: KaSession)
internal fun KtDestructuringDeclaration.isIncompleteDestructuring(): Boolean =
    with(session) {
        val destructuredType = initializer?.expressionType ?: (parent as? KtParameter)?.run { symbol.returnType }
        val classType = destructuredType?.lowerBoundIfFlexible() as? KaClassType
        val dataClass = classType?.takeUnless { it.isMarkedNullable }?.expandedSymbol as? KaNamedClassSymbol
        val primaryConstructor = dataClass
            ?.takeIf { it.isData }
            ?.run { declaredMemberScope.constructors.firstOrNull { it.isPrimary } }
        primaryConstructor != null && entries.size < primaryConstructor.valueParameters.size
    }
