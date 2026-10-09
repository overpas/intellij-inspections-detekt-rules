package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.scopes.KaScope
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtObjectDeclaration

private val JAVA_IO_SERIALIZABLE_CLASS_ID = ClassId.fromString("java/io/Serializable")

private val READ_RESOLVE_NAME = Name.identifier("readResolve")

private val DECLARED_READ_RESOLVE_VISIBILITIES = setOf(
    KaSymbolVisibility.PUBLIC,
    KaSymbolVisibility.PRIVATE,
    KaSymbolVisibility.PROTECTED,
)

private val INHERITED_READ_RESOLVE_VISIBILITIES = setOf(
    KaSymbolVisibility.PUBLIC,
    KaSymbolVisibility.PROTECTED,
)

context(session: KaSession)
internal fun KtObjectDeclaration.isMissingReadResolve(): Boolean =
    with(session) {
        val objectSymbol = symbol
        objectSymbol.defaultType.isSubtypeOf(JAVA_IO_SERIALIZABLE_CLASS_ID) &&
            !objectSymbol.declaredMemberScope.hasReadResolve(DECLARED_READ_RESOLVE_VISIBILITIES) &&
            !objectSymbol.memberScope.hasReadResolve(INHERITED_READ_RESOLVE_VISIBILITIES)
    }

context(session: KaSession)
private fun KaScope.hasReadResolve(visibilities: Set<KaSymbolVisibility>): Boolean =
    with(session) {
        callables(READ_RESOLVE_NAME).any { function ->
            function is KaFunctionSymbol &&
                function.valueParameters.isEmpty() &&
                function.visibility in visibilities &&
                function.returnType.isClassType(StandardClassIds.Any)
        }
    }
