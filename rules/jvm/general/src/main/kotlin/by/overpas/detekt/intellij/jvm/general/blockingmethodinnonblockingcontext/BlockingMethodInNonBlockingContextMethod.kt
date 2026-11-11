package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression

private val STANDARD_PACKAGES = listOf("java.", "javax.", "jakarta.")

private val BLOCKING_EXCEPTIONS = listOf(
    ClassId.fromString("java/lang/InterruptedException"),
    ClassId.fromString("java/io/IOException"),
)

private val NON_BLOCKING_EXCEPTION = ClassId.fromString("java/net/MalformedURLException")

internal class BlockingMethodInNonBlockingContextMethod(
    private val annotations: BlockingMethodInNonBlockingContextAnnotations,
    private val blockingMethods: Set<String>,
) {

    context(session: KaSession)
    fun isBlocking(expression: KtCallExpression): Boolean =
        with(session) {
            val symbol = expression.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            val owner = symbol?.containingDeclaration as? KaClassSymbol
            symbol != null &&
                (
                    isConfiguredBlocking(symbol) ||
                        annotations.isBlocking(symbol, owner) ||
                        symbol.throwsBlockingException(owner)
                    )
        }

    context(session: KaSession)
    private fun isConfiguredBlocking(symbol: KaFunctionSymbol): Boolean =
        blockingMethods.isNotEmpty() &&
            with(session) { sequenceOf(symbol) + symbol.allOverriddenSymbols }
                .any { it.callableId?.run { asSingleFqName().asString() } in blockingMethods }

    context(session: KaSession)
    private fun KaFunctionSymbol.throwsBlockingException(owner: KaClassSymbol?): Boolean =
        with(session) {
            val ownerName = owner?.classId?.run { asSingleFqName().asString() }.orEmpty()
            val blockingExceptions = BLOCKING_EXCEPTIONS.mapNotNull { findClass(it) }
            val thrown = (psi as? PsiMethod)?.run { throwsList.referencedTypes }.orEmpty()
                .mapNotNull { type -> findClass(ClassId.topLevel(FqName(type.canonicalText))) }
            STANDARD_PACKAGES.any { ownerName.startsWith(it) } &&
                thrown.any { exception ->
                    exception.classId != NON_BLOCKING_EXCEPTION &&
                        blockingExceptions.any { exception == it || exception.isSubClassOf(it) }
                }
        }
}
