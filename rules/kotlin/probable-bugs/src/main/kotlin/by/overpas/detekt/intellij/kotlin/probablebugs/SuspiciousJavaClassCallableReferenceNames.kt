package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

private val SUSPICIOUS_JAVA_CLASS_CALLABLE_ID =
    CallableId(FqName("kotlin.jvm"), Name.identifier("javaClass"))

internal fun KtSimpleNameExpression.hasJavaClassName(): Boolean =
    getReferencedNameAsName() == SUSPICIOUS_JAVA_CLASS_CALLABLE_ID.callableName

context(session: KaSession)
internal fun KtSimpleNameExpression.isJavaClassReference(): Boolean =
    with(session) {
        references.filterIsInstance<KtReference>().any { reference ->
            val symbol = reference.resolveToSymbol() as? KaCallableSymbol
            symbol?.callableId == SUSPICIOUS_JAVA_CLASS_CALLABLE_ID
        }
    }
