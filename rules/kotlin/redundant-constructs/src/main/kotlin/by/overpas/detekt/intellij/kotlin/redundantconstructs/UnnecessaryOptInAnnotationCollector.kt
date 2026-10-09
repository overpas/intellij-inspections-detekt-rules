package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotationValue
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaAnnotatedSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExperimentalApi
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.resolution.KtResolvable

private val SUBCLASS_OPT_IN_REQUIRED_CLASS_ID = ClassId.topLevel(FqName("kotlin.SubclassOptInRequired"))
private val INCREMENTS = setOf(KtTokens.PLUSPLUS, KtTokens.MINUSMINUS)

@OptIn(KaExperimentalApi::class, KtExperimentalApi::class)
internal class UnnecessaryOptInAnnotationCollector(private val session: KaSession) : KtTreeVisitorVoid() {

    private val symbols = UnnecessaryOptInAnnotationSymbols(session)

    val foundMarkers = mutableSetOf<ClassId>()

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        foundMarkers += classOrObject.subclassMarkers()
        super.visitClassOrObject(classOrObject)
    }

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        foundMarkers += declaration.declarationMarkers()
        super.visitNamedDeclaration(declaration)
    }

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        foundMarkers += expression.referenceMarkers()
        super.visitReferenceExpression(expression)
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {
        foundMarkers += symbols.markers(roots = emptyList(), types = listOf(with(session) { typeReference.type }))
        super.visitTypeReference(typeReference)
    }

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        val resolved = with(session) { delegate.resolveSymbols() }.filterIsInstance<KaAnnotatedSymbol>()
        foundMarkers += symbols.markers(roots = resolved, types = emptyList())
        super.visitPropertyDelegate(delegate)
    }

    private fun KtClassOrObject.subclassMarkers(): List<ClassId> =
        with(session) {
            superTypeListEntries.asSequence()
                .mapNotNull { entry -> entry.typeReference?.run { type.expandedSymbol } }
                .flatMap { it.annotations[SUBCLASS_OPT_IN_REQUIRED_CLASS_ID] }
                .flatMap { annotation -> annotation.arguments.map { it.expression } }
                .flatMap { value -> (value as? KaAnnotationValue.ArrayValue)?.values ?: listOf(value) }
                .mapNotNull { ((it as? KaAnnotationValue.ClassLiteralValue)?.type as? KaClassType)?.classId }
                .toList()
        }

    private fun KtNamedDeclaration.declarationMarkers(): Set<ClassId> {
        val declaration = takeIf { it is KtFunction || it is KtProperty || it is KtParameter }
        val isOverride = hasModifier(KtTokens.OVERRIDE_KEYWORD)
        return with(session) {
            val callable = declaration?.symbol as? KaCallableSymbol
            val overridden = callable?.takeIf { isOverride }?.run { directlyOverriddenSymbols.toList() }.orEmpty()
            val vararg = (callable as? KaValueParameterSymbol)?.takeIf { it.isVararg }?.varargArrayType
            symbols.markers(roots = overridden, types = listOfNotNull(vararg))
        }
    }

    private fun KtReferenceExpression.referenceMarkers(): Set<ClassId> =
        with(session) {
            val resolved = resolvedSymbols()
            val setters = resolved.filterIsInstance<KaPropertySymbol>().mapNotNull { it.setter }
            val abbreviation = (parent as? KtCallExpression)?.expressionType?.abbreviation
            symbols.markers(
                roots = buildList {
                    addAll(symbols.relatedSymbols(resolved))
                    addAll(setters.takeIf { isWriteAccess() }.orEmpty())
                    abbreviation?.let { add(it.symbol) }
                },
                types = symbols.signatureTypes(resolved),
            )
        }

    private fun KtReferenceExpression.resolvedSymbols(): List<KaSymbol> {
        val resolvable = this as? KtResolvable
        return with(session) {
            val call = resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()
            call?.let { listOf(it.symbol) } ?: resolvable?.run { resolveSymbols().toList() }.orEmpty()
        }
    }

    private fun KtReferenceExpression.isWriteAccess(): Boolean {
        val target = (parent as? KtQualifiedExpression)?.takeIf { it.selectorExpression == this } ?: this
        return when (val context = target.parent) {
            is KtBinaryExpression -> context.left == target && context.operationToken in KtTokens.ALL_ASSIGNMENTS
            is KtUnaryExpression -> context.operationToken in INCREMENTS
            else -> false
        }
    }
}
