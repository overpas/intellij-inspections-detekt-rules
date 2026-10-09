package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeAliasSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaAnnotatedSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

private val REQUIRES_OPT_IN_CLASS_IDS = setOf(
    ClassId.topLevel(FqName("kotlin.RequiresOptIn")),
    ClassId.topLevel(FqName("kotlin.Experimental")),
)

internal class UnnecessaryOptInAnnotationSymbols(private val session: KaSession) {

    fun markers(
        roots: List<KaAnnotatedSymbol>,
        types: List<KaType>,
    ): Set<ClassId> =
        with(session) {
            val nestedTypes = generateSequence(types.toList()) { level ->
                level.filterIsInstance<KaClassType>()
                    .flatMap { type -> type.typeArguments.mapNotNull { it.type } }
                    .takeIf { it.isNotEmpty() }
            }.flatten()
            val typeSymbols = nestedTypes.flatMap { listOfNotNull(it.abbreviation?.symbol, it.expandedSymbol) }
            (roots.asSequence() + typeSymbols)
                .flatMap { root ->
                    generateSequence(
                        root,
                    ) { (it as? KaClassSymbol)?.containingDeclaration }
                }
                .flatMap { symbol -> symbol.annotations.mapNotNull { it.classId } }
                .filter { it.requiresOptIn() }
                .toSet()
        }

    fun relatedSymbols(resolved: List<KaSymbol>): List<KaAnnotatedSymbol> =
        with(session) {
            resolved.flatMap { symbol ->
                val related = when (symbol) {
                    is KaConstructorSymbol -> symbol.containingClassId?.let { findClass(it) }
                    is KaTypeAliasSymbol -> symbol.expandedType.expandedSymbol
                    else -> null
                }
                listOfNotNull(symbol as? KaAnnotatedSymbol, related) +
                    generateSequence(symbol.qualifyingOuter()) { it.qualifyingOuter() }
            }
        }

    fun signatureTypes(resolved: List<KaSymbol>): List<KaType> =
        resolved.filterIsInstance<KaCallableSymbol>().flatMap { symbol ->
            (symbol as? KaFunctionSymbol)?.valueParameters.orEmpty().map { it.returnType } +
                listOfNotNull(symbol.returnType, symbol.receiverParameter?.returnType)
        }

    private fun KaSymbol.qualifyingOuter(): KaNamedClassSymbol? =
        with(session) {
            (containingSymbol as? KaNamedClassSymbol)?.takeIf { outer ->
                outer.classKind.isObject || outer.classId?.outerClassId == null
            }
        }

    private fun ClassId.requiresOptIn(): Boolean {
        val classId = this
        val annotations = with(session) { findClass(classId)?.annotations }
        return annotations != null && REQUIRES_OPT_IN_CLASS_IDS.any { it in annotations }
    }
}
